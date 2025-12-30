package com.portingdeadmods.spaceploitation.datagen;

import com.portingdeadmods.spaceploitation.content.block.CompressorGhostBlock;
import com.portingdeadmods.spaceploitation.content.block.PlanetSimulatorPartBlock;
import com.portingdeadmods.spaceploitation.registries.MJBlocks;
import com.portingdeadmods.spaceploitation.registries.MJItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.Set;

public class MJLootTableProvider extends BlockLootSubProvider {
    public MJLootTableProvider(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        dropSelf(MJBlocks.PLANET_SIMULATOR_CONTROLLER.get());
        dropSelf(MJBlocks.ENERGY_INPUT_BUS.get());
        dropSelf(MJBlocks.ENERGY_OUTPUT_BUS.get());
        dropSelf(MJBlocks.ITEM_INPUT_BUS.get());
        dropSelf(MJBlocks.ITEM_OUTPUT_BUS.get());
        dropSelf(MJBlocks.FLUID_INPUT_BUS.get());
        dropSelf(MJBlocks.FLUID_OUTPUT_BUS.get());
        dropSelf(MJBlocks.TANTALUM_STORAGE_BLOCK.get());
        dropSelf(MJBlocks.PLANET_SIMULATOR_CASING.get());
        dropSelf(MJBlocks.PLANET_SIMULATOR_FRAME.get());
        dropSelf(MJBlocks.COMPRESSOR.get());
        dropSelf(MJBlocks.CREATIVE_POWER.get());

        add(MJBlocks.TANTALUM_ORE.get(), block -> createOreDrop(MJBlocks.TANTALUM_ORE.get(), MJItems.RAW_TANTALUM.get()));
        add(MJBlocks.DEEPSLATE_TANTALUM_ORE.get(), block -> createOreDrop(MJBlocks.DEEPSLATE_TANTALUM_ORE.get(), MJItems.RAW_TANTALUM.get()));

        add(MJBlocks.PLANET_SIMULATOR_PART.get(), block -> createPartDrop(MJBlocks.PLANET_SIMULATOR_PART.get()));
    }

    protected LootTable.Builder createOreDrop(Block block, Item item) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchDispatchTable(
                block,
                this.applyExplosionDecay(
                        block,
                        LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F)))
                                .apply(ApplyBonusCount.addOreBonusCount(registrylookup.getOrThrow(Enchantments.FORTUNE)))
                )
        );
    }

    protected LootTable.Builder createPartDrop(Block block){
        return LootTable.lootTable().withPool(this.applyExplosionCondition(
                block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(MJBlocks.PLANET_SIMULATOR_CASING.get())
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(MJBlocks.PLANET_SIMULATOR_PART.get())
                                        .setProperties(net.minecraft.advancements.critereon.StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(PlanetSimulatorPartBlock.VARIANT, PlanetSimulatorPartBlock.Variant.CASING))))
                        .add(LootItem.lootTableItem(MJBlocks.PLANET_SIMULATOR_CASING.get())
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(MJBlocks.PLANET_SIMULATOR_PART.get())
                                        .setProperties(net.minecraft.advancements.critereon.StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(PlanetSimulatorPartBlock.VARIANT, PlanetSimulatorPartBlock.Variant.PROJECTOR))))
                        .add(LootItem.lootTableItem(MJBlocks.PLANET_SIMULATOR_FRAME.get())
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(MJBlocks.PLANET_SIMULATOR_PART.get())
                                        .setProperties(net.minecraft.advancements.critereon.StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(PlanetSimulatorPartBlock.VARIANT, PlanetSimulatorPartBlock.Variant.FRAME))))
        ));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return MJBlocks.BLOCKS.getEntries().stream()
                .map(entry -> (Block) entry.get())
                .filter(block -> !(block instanceof CompressorGhostBlock))
                //.filter(block -> !(block instanceof PlanetSimulatorPartBlock))
                .toList();
    }
}
