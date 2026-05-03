package com.lw.random_additions.common.mixins.dra;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.brandon3055.draconicevolution.items.tools.DislocatorAdvanced;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DislocatorAdvanced.class)
public class MixinDislocatorAdvanced implements IBauble {
    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.TRINKET;
    }
}
