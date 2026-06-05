package com.lw.random_additions.common.network;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.lw.random_additions.common.init.Mods;
import io.netty.buffer.ByteBuf;
import lumien.randomthings.entitys.EntityTimeAccelerator;
import lumien.randomthings.item.ItemTimeInABottle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Optional;

public class PacketTimeBottle implements IMessage {

    private BlockPos pos;
    private EnumFacing side;
    private float hitX;
    private float hitY;
    private float hitZ;

    public PacketTimeBottle() {}

    public PacketTimeBottle(BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        this.pos = pos;
        this.side = side;
        this.hitX = hitX;
        this.hitY = hitY;
        this.hitZ = hitZ;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.side = EnumFacing.byIndex(buf.readInt());
        this.hitX = buf.readFloat();
        this.hitY = buf.readFloat();
        this.hitZ = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeInt(this.side.getIndex());
        buf.writeFloat(this.hitX);
        buf.writeFloat(this.hitY);
        buf.writeFloat(this.hitZ);
    }

    public static class Handler implements IMessageHandler<PacketTimeBottle, IMessage> {
        @Override
        public IMessage onMessage(PacketTimeBottle message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
            MinecraftServer server = player.getServer();
            if (server != null) {
                server.addScheduledTask(() -> handle(player, message));
            }
            return null;
        }

        private void handle(EntityPlayer player, PacketTimeBottle msg) {
            World world = player.world;
            BlockPos pos = msg.pos;

            ItemStack bottle = findTimeBottle(player);
            if (bottle.isEmpty()) return;

            long storedTime = ItemTimeInABottle.getStoredTime(bottle, player);

            Optional<EntityTimeAccelerator> o = world.getEntitiesWithinAABB(
                    EntityTimeAccelerator.class,
                    new AxisAlignedBB(pos).grow(0.2)
            ).stream().findFirst();

            if (o.isPresent()) {
                EntityTimeAccelerator eta = o.get();
                int currentRate = eta.getTimeRate();
                if (currentRate >= 32) return;

                int nextRate = currentRate * 2;
                long timeRequired = nextRate / 2 * 20 * 30;

                if (storedTime >= timeRequired || player.capabilities.isCreativeMode) {
                    int usedUpTime = 600 - eta.getRemainingTime();
                    int timeAdded = (nextRate * usedUpTime - currentRate * usedUpTime) / nextRate;

                    if (!player.capabilities.isCreativeMode) {
                        ItemTimeInABottle.setStoredTime(bottle, player, storedTime - timeRequired);
                    }

                    eta.setTimeRate(nextRate);
                    eta.setRemainingTime(eta.getRemainingTime() + timeAdded);

                    float pitch;
                    switch (nextRate) {
                        case 2:  pitch = 0.793701F; break;
                        case 4:  pitch = 0.890899F; break;
                        case 8:  pitch = 1.059463F; break;
                        case 16: pitch = 0.943874F; break;
                        case 32: pitch = 0.890899F; break;
                        default: pitch = 1.0F;
                    }
                    world.playSound(null, pos, SoundEvents.BLOCK_NOTE_HARP, SoundCategory.BLOCKS, 0.5F, pitch);
                }
            } else {
                if (storedTime >= 600 || player.capabilities.isCreativeMode) {
                    if (!player.capabilities.isCreativeMode) {
                        ItemTimeInABottle.setStoredTime(bottle, player, storedTime - 600);
                    }

                    EntityTimeAccelerator n = new EntityTimeAccelerator(
                            world, pos,
                            pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D
                    );
                    n.setTimeRate(1);
                    n.setRemainingTime(600);
                    world.playSound(null, pos, SoundEvents.BLOCK_NOTE_HARP, SoundCategory.BLOCKS, 0.5F, 0.749154F);
                    world.spawnEntity(n);
                }
            }
        }

        private ItemStack findTimeBottle(EntityPlayer player) {

            ItemStack main = player.getHeldItemMainhand();

            if (main.getItem() instanceof ItemTimeInABottle) return main;

            ItemStack off = player.getHeldItemOffhand();

            if (off.getItem() instanceof ItemTimeInABottle) return off;

            for (ItemStack stack : player.inventory.mainInventory) {
                if (stack.getItem() instanceof ItemTimeInABottle) return stack;
            }

            if (Mods.BAUBLES.isLoaded()) {
                IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
                if (baubles != null) {
                    for (int i = 0; i < baubles.getSlots(); i++) {
                        ItemStack stack = baubles.getStackInSlot(i);
                        if (stack.getItem() instanceof ItemTimeInABottle) return stack;
                    }
                }
            }

            return ItemStack.EMPTY;
        }
    }
}
