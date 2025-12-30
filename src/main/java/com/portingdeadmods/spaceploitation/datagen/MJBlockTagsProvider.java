package com.portingdeadmods.spaceploitation.datagen;

import com.portingdeadmods.spaceploitation.Spaceploitation;
import com.portingdeadmods.spaceploitation.registries.MJBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MJBlockTagsProvider extends BlockTagsProvider {

    public MJBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Spaceploitation.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(MJBlocks.TANTALUM_STORAGE_BLOCK.get())
                .add(MJBlocks.TANTALUM_ORE.get())
                .add(MJBlocks.DEEPSLATE_TANTALUM_ORE.get())
                .add(MJBlocks.COMPRESSOR.get())
                .add(MJBlocks.PLANET_SIMULATOR_FRAME.get())
                .add(MJBlocks.PLANET_SIMULATOR_CASING.get())
                .add(MJBlocks.PLANET_SIMULATOR_CONTROLLER.get())
                .add(MJBlocks.PLANET_SIMULATOR_PART.get())
                .add(MJBlocks.ENERGY_INPUT_BUS.get())
                .add(MJBlocks.ENERGY_OUTPUT_BUS.get())
                .add(MJBlocks.ITEM_INPUT_BUS.get())
                .add(MJBlocks.ITEM_OUTPUT_BUS.get())
                .add(MJBlocks.FLUID_INPUT_BUS.get())
                .add(MJBlocks.FLUID_OUTPUT_BUS.get());
        
        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(MJBlocks.TANTALUM_STORAGE_BLOCK.get())
                .add(MJBlocks.TANTALUM_ORE.get())
                .add(MJBlocks.DEEPSLATE_TANTALUM_ORE.get())
                .add(MJBlocks.COMPRESSOR.get())
                .add(MJBlocks.PLANET_SIMULATOR_FRAME.get())
                .add(MJBlocks.PLANET_SIMULATOR_CASING.get())
                .add(MJBlocks.PLANET_SIMULATOR_CONTROLLER.get())
                .add(MJBlocks.PLANET_SIMULATOR_PART.get())
                .add(MJBlocks.ENERGY_INPUT_BUS.get())
                .add(MJBlocks.ENERGY_OUTPUT_BUS.get())
                .add(MJBlocks.ITEM_INPUT_BUS.get())
                .add(MJBlocks.ITEM_OUTPUT_BUS.get())
                .add(MJBlocks.FLUID_INPUT_BUS.get())
                .add(MJBlocks.FLUID_OUTPUT_BUS.get());
        
        tag(Tags.Blocks.STORAGE_BLOCKS)
                .add(MJBlocks.TANTALUM_STORAGE_BLOCK.get());
        
        tag(Tags.Blocks.ORES)
                .add(MJBlocks.TANTALUM_ORE.get())
                .add(MJBlocks.DEEPSLATE_TANTALUM_ORE.get());
        
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/tantalum")))
                .add(MJBlocks.TANTALUM_STORAGE_BLOCK.get());
        
        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/tantalum")))
                .add(MJBlocks.TANTALUM_ORE.get())
                .add(MJBlocks.DEEPSLATE_TANTALUM_ORE.get());
    }
}
