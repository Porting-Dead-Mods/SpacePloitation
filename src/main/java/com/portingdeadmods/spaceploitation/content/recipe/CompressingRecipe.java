package com.portingdeadmods.spaceploitation.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.portingdeadmods.portingdeadlibs.api.recipes.IngredientWithCount;
import com.portingdeadmods.spaceploitation.registries.MJRecipes;
import com.portingdeadmods.portingdeadlibs.api.recipes.PDLRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record CompressingRecipe(IngredientWithCount ingredient, int duration, ItemStack result) implements PDLRecipe<SingleRecipeInput> {
    public static final RecipeType<CompressingRecipe> TYPE = MJRecipes.COMPRESSING_TYPE;

    @Override
    public boolean matches(SingleRecipeInput singleRecipeInput, Level level) {
        return ingredient.test(singleRecipeInput.getItem(0));
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.result.copy();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return TYPE;
    }

    public static final class Serializer implements RecipeSerializer<CompressingRecipe> {
        public static final MapCodec<CompressingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                IngredientWithCount.CODEC.fieldOf("ingredient").forGetter(CompressingRecipe::ingredient),
                Codec.INT.fieldOf("duration").forGetter(CompressingRecipe::duration),
                ItemStack.CODEC.fieldOf("result").forGetter(CompressingRecipe::result)
        ).apply(inst, CompressingRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, CompressingRecipe> STREAM_CODEC = StreamCodec.composite(
                IngredientWithCount.STREAM_CODEC,
                CompressingRecipe::ingredient,
                ByteBufCodecs.INT,
                CompressingRecipe::duration,
                ItemStack.STREAM_CODEC,
                CompressingRecipe::result,
                CompressingRecipe::new
        );
        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public @NotNull MapCodec<CompressingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, CompressingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

}
