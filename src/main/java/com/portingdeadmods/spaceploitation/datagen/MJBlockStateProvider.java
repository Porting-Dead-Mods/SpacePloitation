package com.portingdeadmods.spaceploitation.datagen;

import com.portingdeadmods.spaceploitation.Spaceploitation;
import com.portingdeadmods.spaceploitation.content.block.CompressorBlock;
import com.portingdeadmods.spaceploitation.content.block.PlanetSimulatorPartBlock;
import com.portingdeadmods.spaceploitation.registries.MJBlocks;
import com.portingdeadmods.portingdeadlibs.api.datagen.ModelBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.jetbrains.annotations.NotNull;

public class MJBlockStateProvider extends BlockStateProvider {

    public MJBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Spaceploitation.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(MJBlocks.TANTALUM_STORAGE_BLOCK.get(),
            cubeAll(MJBlocks.TANTALUM_STORAGE_BLOCK.get()));
        simpleBlockWithItem(MJBlocks.TANTALUM_ORE.get(),
            cubeAll(MJBlocks.TANTALUM_ORE.get()));
        simpleBlockWithItem(MJBlocks.DEEPSLATE_TANTALUM_ORE.get(),
            cubeAll(MJBlocks.DEEPSLATE_TANTALUM_ORE.get()));
        simpleBlockWithItem(MJBlocks.PLANET_SIMULATOR_CASING.get(),
                cubeAll(MJBlocks.PLANET_SIMULATOR_CASING.get()));
        simpleBlockWithItem(MJBlocks.PLANET_SIMULATOR_FRAME.get(),
                cubeAll(MJBlocks.PLANET_SIMULATOR_FRAME.get()));
        simpleBlockWithItem(MJBlocks.CREATIVE_POWER.get(),
                cubeAll(MJBlocks.CREATIVE_POWER.get()));

        getVariantBuilder(MJBlocks.PLANET_SIMULATOR_PART.get())
                .partialState().with(PlanetSimulatorPartBlock.VARIANT, PlanetSimulatorPartBlock.Variant.CASING)
                .modelForState().modelFile(cubeAll(MJBlocks.PLANET_SIMULATOR_CASING.get())).addModel()
                .partialState().with(PlanetSimulatorPartBlock.VARIANT, PlanetSimulatorPartBlock.Variant.FRAME)
                .modelForState().modelFile(cubeAll(MJBlocks.PLANET_SIMULATOR_FRAME.get())).addModel()
                .partialState().with(PlanetSimulatorPartBlock.VARIANT, PlanetSimulatorPartBlock.Variant.PROJECTOR)
                .modelForState().modelFile(models().getExistingFile(Spaceploitation.rl("block/planet_simulator_projector"))).addModel();

        builder(MJBlocks.PLANET_SIMULATOR_CONTROLLER)
                .defaultTexture(this.blockTexture(MJBlocks.PLANET_SIMULATOR_CASING.get()))
                .front(this::blockTexture)
                .horizontalFacing()
                .create();

        builder(MJBlocks.ENERGY_INPUT_BUS)
                .defaultTexture(this.blockTexture(MJBlocks.PLANET_SIMULATOR_CASING.get()))
                .front(Spaceploitation.rl("block/planet_simulator_bus_input_energy"))
                .create();
        builder(MJBlocks.ENERGY_OUTPUT_BUS)
                .defaultTexture(this.blockTexture(MJBlocks.PLANET_SIMULATOR_CASING.get()))
                .front(Spaceploitation.rl("block/planet_simulator_bus_output_energy"))
                .create();
        builder(MJBlocks.ITEM_INPUT_BUS)
                .defaultTexture(this.blockTexture(MJBlocks.PLANET_SIMULATOR_CASING.get()))
                .front(Spaceploitation.rl("block/planet_simulator_bus_input_item"))
                .create();
        builder(MJBlocks.ITEM_OUTPUT_BUS)
                .defaultTexture(this.blockTexture(MJBlocks.PLANET_SIMULATOR_CASING.get()))
                .front(Spaceploitation.rl("block/planet_simulator_bus_output_item"))
                .create();
        builder(MJBlocks.FLUID_INPUT_BUS)
                .defaultTexture(this.blockTexture(MJBlocks.PLANET_SIMULATOR_CASING.get()))
                .front(Spaceploitation.rl("block/planet_simulator_bus_input_fluid"))
                .create();
        builder(MJBlocks.FLUID_OUTPUT_BUS)
                .defaultTexture(this.blockTexture(MJBlocks.PLANET_SIMULATOR_CASING.get()))
                .front(Spaceploitation.rl("block/planet_simulator_bus_output_fluid"))
                .create();

    }

    private @NotNull ModelBuilder builder(DeferredBlock<? extends Block> block) {
        return new ModelBuilder(block.get(), this);
    }
}
