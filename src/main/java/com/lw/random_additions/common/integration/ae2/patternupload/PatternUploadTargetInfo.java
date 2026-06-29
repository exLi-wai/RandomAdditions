package com.lw.random_additions.common.integration.ae2.patternupload;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PatternUploadTargetInfo {

    private final String name;
    private final ItemStack icon;
    private final boolean full;
    private final int freeCount;
    private final int totalCount;
    private final String searchText;

    public PatternUploadTargetInfo(String name, ItemStack icon, boolean full) {
        this(name, icon, full, full ? 0 : 1, 1, name);
    }

    public PatternUploadTargetInfo(String name, ItemStack icon, boolean full, int freeCount, int totalCount, String searchText) {
        this.name = name;
        this.icon = icon == null ? ItemStack.EMPTY : icon.copy();
        this.full = full;
        this.freeCount = freeCount;
        this.totalCount = totalCount;
        this.searchText = searchText == null ? name : searchText;
    }

    public String getName() {
        return name;
    }

    public ItemStack getIcon() {
        return icon.copy();
    }

    public boolean isFull() {
        return full;
    }

    public int getFreeCount() {
        return freeCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public String getSearchText() {
        return searchText;
    }

    public void writeTo(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.name);
        ByteBufUtils.writeItemStack(buf, this.icon);
        buf.writeBoolean(this.full);
        buf.writeInt(this.freeCount);
        buf.writeInt(this.totalCount);
        ByteBufUtils.writeUTF8String(buf, this.searchText);
    }

    public static PatternUploadTargetInfo readFrom(ByteBuf buf) {
        String name = ByteBufUtils.readUTF8String(buf);
        ItemStack icon = ByteBufUtils.readItemStack(buf);
        boolean full = buf.readBoolean();
        int freeCount = buf.readInt();
        int totalCount = buf.readInt();
        String searchText = ByteBufUtils.readUTF8String(buf);
        return new PatternUploadTargetInfo(name, icon, full, freeCount, totalCount, searchText);
    }
}
