package com.lw.random_additions.network;

import com.lw.random_additions.util.aeUtil;
import io.netty.buffer.ByteBuf;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.features.IWirelessTermRegistry;
import appeng.api.networking.IGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.localization.PlayerMessages;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.me.helpers.PlayerSource;
import appeng.tile.misc.TileSecurityStation;
import appeng.util.item.AEItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WirelessInput implements IMessage {

    private int handSlot;
    private int amount;

    public WirelessInput() {}

    public WirelessInput(int handSlot, int amount) {
        this.handSlot = handSlot;
        this.amount = amount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.handSlot = buf.readInt();
        this.amount = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.handSlot);
        buf.writeInt(this.amount);
    }

    public static class Handler implements IMessageHandler<WirelessInput, IMessage> {

        private static final Map<UUID, Long> cooldownMap = new ConcurrentHashMap<>();
        private static final long COOLDOWN_MS = 100;

        @Override
        public IMessage onMessage(WirelessInput message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
            MinecraftServer server = player.getServer();
            UUID playerUUID = player.getUniqueID();

            if (!checkCooldown(playerUUID)) {
                return null;
            }

            server.addScheduledTask(() -> {
                ItemStack handItem = message.handSlot == 0 ? player.getHeldItemMainhand() : player.getHeldItemOffhand();

                if (handItem.isEmpty()) {
                    return;
                }

                int depositAmount = message.amount;

                if (depositAmount <= 0 || depositAmount > handItem.getCount()) {
                    depositAmount = handItem.getCount();
                }

                ItemStack toDeposit = handItem.copy();
                toDeposit.setCount(depositAmount);
                boolean success = findAndDeposit(player, toDeposit);

                if (depositAmount <= 0 || depositAmount > handItem.getCount()) {
                    depositAmount = handItem.getCount();
                }

                if (success) {
                    handItem.shrink(depositAmount);
                    if (handItem.getCount() <= 0) {
                        if (message.handSlot == 0) {
                            player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
                        } else {
                            player.inventory.offHandInventory.set(0, ItemStack.EMPTY);
                        }
                    }
                }
            });
            return null;
        }

        private boolean checkCooldown(UUID playerId) {
            long currentTime = System.currentTimeMillis();
            Long lastTime = cooldownMap.get(playerId);

            if (lastTime != null && currentTime - lastTime < COOLDOWN_MS) {
                return false;
            }

            cooldownMap.put(playerId, currentTime);
            return true;
        }

        private boolean findAndDeposit(EntityPlayer player, ItemStack toDeposit) {
            ItemStack terminal = aeUtil.getWirelessTerminalFromPlayer(player);
            if (terminal.isEmpty()) {
                return false;
            }
            return tryDepositWithTerminal(player, terminal, toDeposit);
        }

        private boolean tryDepositWithTerminal(EntityPlayer player, ItemStack terminal, ItemStack toDeposit) {
            if (!(terminal.getItem() instanceof ToolWirelessTerminal)) {
                return false;
            }
            return depositToNetwork(player, terminal, toDeposit, new BlockPos(-1, -1, -1));
        }

        private boolean depositToNetwork(EntityPlayer player, ItemStack terminal, ItemStack toDeposit, BlockPos terminalPos) {
            IWirelessTermRegistry registry = AEApi.instance().registries().wireless();
            IWirelessTermHandler handler = registry.getWirelessTerminalHandler(terminal);
            if (handler == null) {
                return false;
            }

            if (!handler.hasPower(player, 10.0F, terminal)) {
                player.sendMessage(PlayerMessages.DeviceNotPowered.get());
                return false;
            }

            IGrid grid = aeUtil.getGridFromTerminal(terminal, player, terminalPos);
            if (grid == null) {
                player.sendMessage(PlayerMessages.DeviceNotLinked.get());
                return false;
            }

            if (!aeUtil.securityCheck(player, grid, SecurityPermissions.INJECT)) {
                return false;
            }

            IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
            if (storageGrid == null) {
                return false;
            }

            String encryptionKey = handler.getEncryptionKey(terminal);
            long parsedKey;
            try {
                parsedKey = Long.parseLong(encryptionKey);
            } catch (NumberFormatException e) {
                return false;
            }
            ILocatable securityStation = AEApi.instance().registries().locatable().getLocatableBy(parsedKey);
            if (!(securityStation instanceof TileSecurityStation)) {
                return false;
            }
            TileSecurityStation t = (TileSecurityStation) securityStation;

            AEItemStack aeItemStack = AEItemStack.fromItemStack(toDeposit);
            if (aeItemStack == null) {
                return false;
            }

            IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
            IMEInventory<IAEItemStack> inventory = storageGrid.getInventory(channel);
            if (inventory == null) {
                return false;
            }

            IAEItemStack remaining = inventory.injectItems(
                    aeItemStack,
                    Actionable.MODULATE,
                    new PlayerSource(player, t)
            );

            long deposited = toDeposit.getCount() - (remaining != null ? remaining.getStackSize() : 0);

            if (deposited > 0) {
                handler.usePower(player, 10.0 * deposited / 64.0, terminal);
                return true;
            }
            return false;
        }
    }
}
