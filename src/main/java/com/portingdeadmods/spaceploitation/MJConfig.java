package com.portingdeadmods.spaceploitation;

import com.portingdeadmods.portingdeadlibs.api.config.ConfigValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class MJConfig {
    @ConfigValue(name = "Compressor Energy Capacity", comment = "Energy Capacity of the Compressor")
    public static int compressorEnergyCapacity = 10000;

    @ConfigValue(name = "Compressor Energy Usage", comment = "Energy Usage of the Compressor")
    public static int compressorEnergyUsage = 10;
}
