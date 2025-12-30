package com.portingdeadmods.spaceploitation.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.portingdeadmods.spaceploitation.data.PlanetType;
import com.portingdeadmods.spaceploitation.registries.MJRecipes;
import com.portingdeadmods.spaceploitation.registries.MJRegistries;
import com.portingdeadmods.portingdeadlibs.api.recipes.IngredientWithCount;
import com.portingdeadmods.portingdeadlibs.api.recipes.PDLRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record PlanetPowerRecipe(
        ResourceKey<PlanetType> planetType,
        List<IngredientWithCount> catalysts,
        List<IngredientWithCount> inputs,
        Optional<FluidIngredient> fluidInput,
        int energyPerTick,
        int duration
) implements PDLRecipe<SingleRecipeInput> {
    public static final RecipeType<PlanetPowerRecipe> TYPE = MJRecipes.PLANET_POWER_TYPE;

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return TYPE;
    }

    public static final class Serializer implements RecipeSerializer<PlanetPowerRecipe> {
        public static final MapCodec<PlanetPowerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                ResourceKey.codec(MJRegistries.PLANET_TYPE_KEY).fieldOf("planet_type").forGetter(PlanetPowerRecipe::planetType),
                IngredientWithCount.CODEC.listOf().optionalFieldOf("catalysts", List.of()).forGetter(PlanetPowerRecipe::catalysts),
                IngredientWithCount.CODEC.listOf().optionalFieldOf("inputs", List.of()).forGetter(PlanetPowerRecipe::inputs),
                FluidIngredient.CODEC.optionalFieldOf("fluid_input").forGetter(PlanetPowerRecipe::fluidInput),
                Codec.INT.fieldOf("energy_per_tick").forGetter(PlanetPowerRecipe::energyPerTick),
                Codec.INT.fieldOf("duration").forGetter(PlanetPowerRecipe::duration)
        ).apply(inst, PlanetPowerRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PlanetPowerRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public PlanetPowerRecipe decode(RegistryFriendlyByteBuf buf) {
                ResourceKey<PlanetType> planetType = ResourceKey.streamCodec(MJRegistries.PLANET_TYPE_KEY).decode(buf);
                List<IngredientWithCount> catalysts = IngredientWithCount.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
                List<IngredientWithCount> inputs = IngredientWithCount.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
                Optional<FluidIngredient> fluidInput = ByteBufCodecs.optional(FluidIngredient.STREAM_CODEC).decode(buf);
                int energyPerTick = ByteBufCodecs.INT.decode(buf);
                int duration = ByteBufCodecs.INT.decode(buf);
                return new PlanetPowerRecipe(planetType, catalysts, inputs, fluidInput, energyPerTick, duration);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, PlanetPowerRecipe recipe) {
                ResourceKey.streamCodec(MJRegistries.PLANET_TYPE_KEY).encode(buf, recipe.planetType());
                IngredientWithCount.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.catalysts());
                IngredientWithCount.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.inputs());
                ByteBufCodecs.optional(FluidIngredient.STREAM_CODEC).encode(buf, recipe.fluidInput());
                ByteBufCodecs.INT.encode(buf, recipe.energyPerTick());
                ByteBufCodecs.INT.encode(buf, recipe.duration());
            }
        };

        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public @NotNull MapCodec<PlanetPowerRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, PlanetPowerRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
