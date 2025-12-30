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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record PlanetSimulatorRecipe(
        ResourceKey<PlanetType> planetType,
        List<IngredientWithCount> catalysts,
        List<IngredientWithCount> inputs,
        Optional<FluidIngredient> fluidInput,
        int energyPerTick,
        int duration,
        List<WeightedOutput> outputs
) implements PDLRecipe<SingleRecipeInput> {
    public static final RecipeType<PlanetSimulatorRecipe> TYPE = MJRecipes.PLANET_SIMULATOR_TYPE;

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider provider) {
        return outputs.isEmpty() ? ItemStack.EMPTY : 
               (outputs.get(0).itemStack().isPresent() ? outputs.get(0).itemStack().get() : ItemStack.EMPTY);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return TYPE;
    }

    public record WeightedOutput(Optional<ItemStack> itemStack, Optional<FluidStack> fluidStack, float chance) {
        public static final Codec<WeightedOutput> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ItemStack.CODEC.optionalFieldOf("item").forGetter(WeightedOutput::itemStack),
                FluidStack.OPTIONAL_CODEC.optionalFieldOf("fluid").forGetter(WeightedOutput::fluidStack),
                Codec.FLOAT.fieldOf("chance").forGetter(WeightedOutput::chance)
        ).apply(inst, WeightedOutput::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, WeightedOutput> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(ItemStack.STREAM_CODEC),
                WeightedOutput::itemStack,
                ByteBufCodecs.optional(FluidStack.STREAM_CODEC),
                WeightedOutput::fluidStack,
                ByteBufCodecs.FLOAT,
                WeightedOutput::chance,
                WeightedOutput::new
        );
    }

    public static final class Serializer implements RecipeSerializer<PlanetSimulatorRecipe> {
        public static final MapCodec<PlanetSimulatorRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                ResourceKey.codec(MJRegistries.PLANET_TYPE_KEY).fieldOf("planet_type").forGetter(PlanetSimulatorRecipe::planetType),
                IngredientWithCount.CODEC.listOf().optionalFieldOf("catalysts", List.of()).forGetter(PlanetSimulatorRecipe::catalysts),
                IngredientWithCount.CODEC.listOf().optionalFieldOf("inputs", List.of()).forGetter(PlanetSimulatorRecipe::inputs),
                FluidIngredient.CODEC.optionalFieldOf("fluid_input").forGetter(PlanetSimulatorRecipe::fluidInput),
                Codec.INT.fieldOf("energy_per_tick").forGetter(PlanetSimulatorRecipe::energyPerTick),
                Codec.INT.fieldOf("duration").forGetter(PlanetSimulatorRecipe::duration),
                WeightedOutput.CODEC.listOf().fieldOf("outputs").forGetter(PlanetSimulatorRecipe::outputs)
        ).apply(inst, PlanetSimulatorRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PlanetSimulatorRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public PlanetSimulatorRecipe decode(RegistryFriendlyByteBuf buf) {
                ResourceKey<PlanetType> planetType = ResourceKey.streamCodec(MJRegistries.PLANET_TYPE_KEY).decode(buf);
                List<IngredientWithCount> catalysts = IngredientWithCount.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
                List<IngredientWithCount> inputs = IngredientWithCount.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
                Optional<FluidIngredient> fluidInput = ByteBufCodecs.optional(FluidIngredient.STREAM_CODEC).decode(buf);
                int energyPerTick = ByteBufCodecs.INT.decode(buf);
                int duration = ByteBufCodecs.INT.decode(buf);
                List<WeightedOutput> outputs = WeightedOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
                return new PlanetSimulatorRecipe(planetType, catalysts, inputs, fluidInput, energyPerTick, duration, outputs);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, PlanetSimulatorRecipe recipe) {
                ResourceKey.streamCodec(MJRegistries.PLANET_TYPE_KEY).encode(buf, recipe.planetType());
                IngredientWithCount.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.catalysts());
                IngredientWithCount.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.inputs());
                ByteBufCodecs.optional(FluidIngredient.STREAM_CODEC).encode(buf, recipe.fluidInput());
                ByteBufCodecs.INT.encode(buf, recipe.energyPerTick());
                ByteBufCodecs.INT.encode(buf, recipe.duration());
                WeightedOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.outputs());
            }
        };

        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public @NotNull MapCodec<PlanetSimulatorRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, PlanetSimulatorRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
