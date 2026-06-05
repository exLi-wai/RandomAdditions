package com.lw.random_additions.common.integration.jei;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientRegistry;

import javax.annotation.Nonnull;

@JEIPlugin
public final class JeiPlugin implements IModPlugin {

    private static IJeiRuntime runtime;
    private static IIngredientRegistry ingredientRegistry;

    public static IJeiRuntime getRuntime() {
        return runtime;
    }

    public static IIngredientRegistry getIngredientRegistry() {
        return ingredientRegistry;
    }

    @Override
    public void register(final IModRegistry registry) {
        ingredientRegistry = registry.getIngredientRegistry();
    }

    @Override
    public void onRuntimeAvailable(@Nonnull final IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }
}
