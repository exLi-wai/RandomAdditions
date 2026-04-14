package com.lw.random_additions.common.mixins.thaumcraft;

import com.lw.random_additions.common.config.RandomAdditionsConfig;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.tiles.crafting.TileCrucible;

@Mixin(TileCrucible.class)
public class MixinTileCrucible {

    @ModifyVariable(
            method = "attemptSmelt",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/common/lib/crafting/ThaumcraftCraftingManager;getObjectTags(Lnet/minecraft/item/ItemStack;)Lthaumcraft/api/aspects/AspectList;",
                    shift = At.Shift.BY,
                    by = 2
            ),
            name = "ot"
    )
    public AspectList onAttemptSmelt(AspectList original, ItemStack item){
        if (RandomAdditionsConfig.crucibleWhitelist(item)) {
            return null;
        }
        return original;
    }
}
