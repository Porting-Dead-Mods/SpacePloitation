package com.portingdeadmods.spaceploitation.datagen;

import com.portingdeadmods.portingdeadlibs.api.config.PDLConfigHelper;
import com.portingdeadmods.spaceploitation.MJConfig;
import com.portingdeadmods.spaceploitation.Spaceploitation;
import com.portingdeadmods.spaceploitation.registries.MJItems;
import com.portingdeadmods.spaceploitation.registries.MJTranslations;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class MJLangProvider extends LanguageProvider {

    public MJLangProvider(PackOutput output) {
        super(output, Spaceploitation.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        PDLConfigHelper.generateConfigNames(MJConfig.class, Spaceploitation.MODID, this::add);

        add("itemGroup.spaceploitation", "Mod Jam");
        
        add("block.spaceploitation.tantalum_storage_block", "Block of Tantalum");
        add("block.spaceploitation.tantalum_ore", "Tantalum Ore");
        add("block.spaceploitation.deepslate_tantalum_ore", "Deepslate Tantalum Ore");
        add("block.spaceploitation.compressor", "Compressor");
        add("block.spaceploitation.creative_power", "Creative Power Source");
        add("block.spaceploitation.planet_simulator_controller", "Planet Simulator Controller");
        add("block.spaceploitation.planet_simulator_part", "Planet Simulator Part");
        add("block.spaceploitation.planet_simulator_casing", "Planet Simulator Casing");
        add("block.spaceploitation.planet_simulator_frame", "Planet Simulator Frame");
        
        add("block.spaceploitation.energy_input_bus", "Energy Input Bus");
        add("block.spaceploitation.energy_output_bus", "Energy Output Bus");
        add("block.spaceploitation.item_input_bus", "Item Input Bus");
        add("block.spaceploitation.item_output_bus", "Item Output Bus");
        add("block.spaceploitation.fluid_input_bus", "Fluid Input Bus");
        add("block.spaceploitation.fluid_output_bus", "Fluid Output Bus");
        
        add("item.spaceploitation.raw_tantalum", "Raw Tantalum");
        add("item.spaceploitation.tantalum_ingot", "Tantalum Ingot");
        add("item.spaceploitation.tantalum_nugget", "Tantalum Nugget");
        add("item.spaceploitation.tantalum_sheet", "Tantalum Sheet");
        addItem(MJItems.PLANET_CARD, "Planet Card");
        addItem(MJItems.TINTED_PLANET_CARD, "Planet Card");
        add("item.spaceploitation.planet_card_spaceploitation_earth", "Earth Planet Card");
        add("item.spaceploitation.planet_card_spaceploitation_mars", "Mars Planet Card");
        add("item.spaceploitation.planet_card_spaceploitation_venus", "Venus Planet Card");
        add("item.spaceploitation.planet_card_spaceploitation_blackhole", "Black Hole Card");
        addItem(MJItems.ENERGY_UPGRADE, "Energy Upgrade");
        addItem(MJItems.SPEED_UPGRADE, "Speed Upgrade");
        addItem(MJItems.LUCK_UPGRADE, "Luck Upgrade");
        addItem(MJItems.GUIDE, "ModJam Guide");
        
        add("tooltip.spaceploitation.upgrade.energy", "Energy Efficiency");
        add("tooltip.spaceploitation.upgrade.speed", "Processing Speed");
        add("tooltip.spaceploitation.upgrade.luck", "Bonus Output Chance");
        add("tooltip.spaceploitation.no_upgrades", "No upgrades installed");
        
        add("spaceploitation.jei.multiblock.title", "Multiblock Assembly");
        add("spaceploitation.jei.multiblock.component", "Required Component");
        add("spaceploitation.jei.grinding", "Grinding");
        add("jei.spaceploitation.category.planet_simulator", "Planet Simulator");
        add("jei.spaceploitation.category.planet_power", "Planet Power Generation");
        add("spaceploitation.jei.toggle_exploded_view", "Show Exploded View");
        add("spaceploitation.jei.toggle_condensed_view", "Show Condensed View");
        add("spaceploitation.jei.all_layers_mode", "Show All Layers");
        add("spaceploitation.jei.single_layer_mode", "Show Single Layer");
        add("spaceploitation.jei.layer_up", "Layer Up");
        add("spaceploitation.jei.layer_down", "Layer Down");
        
        add("container.spaceploitation.compressor", "Compressor");
        add("container.spaceploitation.item_input_bus", "Item Input Bus");
        add("container.spaceploitation.item_output_bus", "Item Output Bus");
        add("container.spaceploitation.energy_input_bus", "Energy Input Bus");
        add("container.spaceploitation.energy_output_bus", "Energy Output Bus");
        add("container.spaceploitation.fluid_input_bus", "Fluid Input Bus");
        add("container.spaceploitation.fluid_output_bus", "Fluid Output Bus");
        add("jei.spaceploitation.category.compressing", "Compressing");
        
        add("jade.spaceploitation.recipe_progress", "Progress: %s / %s");
        add("jade.spaceploitation.energy_per_tick", "Energy: %s FE/t");
        add("jade.spaceploitation.total_energy", "Total Energy: %s / %s FE");
        add("jade.spaceploitation.multiblock_not_formed", "Multiblock Not Formed");
        
        add("redstone_signal_type.portingdeadlibs.ignored", "Ignored");
        add("redstone_signal_type.portingdeadlibs.low_signal", "Low Signal");
        add("redstone_signal_type.portingdeadlibs.high_signal", "High Signal");
        
        add("spaceploitation.guide.name", "ModJam Guide");

        MJTranslations.TRANSLATIONS.getDefaultTranslations().forEach(this::add);

    }
}
