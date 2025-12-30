package com.portingdeadmods.spaceploitation.registries;

import com.portingdeadmods.spaceploitation.Spaceploitation;
import com.portingdeadmods.spaceploitation.content.recipe.CompressingRecipe;
import com.portingdeadmods.spaceploitation.content.recipe.PlanetPowerRecipe;
import com.portingdeadmods.spaceploitation.content.recipe.PlanetSimulatorRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MJRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Spaceploitation.MODID);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Spaceploitation.MODID);

    // Recipe Types - must be registered to avoid NPE in GuideMe when AE2 is installed
    public static final RecipeType<CompressingRecipe> COMPRESSING_TYPE = registerType("compressing");
    public static final RecipeType<PlanetSimulatorRecipe> PLANET_SIMULATOR_TYPE = registerType("planet_simulator");
    public static final RecipeType<PlanetPowerRecipe> PLANET_POWER_TYPE = registerType("planet_power");

    // Recipe Serializers
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CompressingRecipe>> COMPRESSING =
            RECIPE_SERIALIZERS.register("compressing", () -> CompressingRecipe.Serializer.INSTANCE);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PlanetSimulatorRecipe>> PLANET_SIMULATOR =
            RECIPE_SERIALIZERS.register("planet_simulator", () -> PlanetSimulatorRecipe.Serializer.INSTANCE);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PlanetPowerRecipe>> PLANET_POWER =
            RECIPE_SERIALIZERS.register("planet_power", () -> PlanetPowerRecipe.Serializer.INSTANCE);

    private static <T extends Recipe<?>> RecipeType<T> registerType(String id) {
        RecipeType<T> type = RecipeType.simple(Spaceploitation.rl(id));
        RECIPE_TYPES.register(id, () -> type);
        return type;
    }
}
