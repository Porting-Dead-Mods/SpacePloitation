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

    @ConfigValue(name = "Black Hole Effect Radius", comment = "Radius of gravity and damage effects around active black holes (in blocks)")
    public static double blackHoleEffectRadius = 4.0;

    @ConfigValue(name = "Black Hole Gravity Strength", comment = "Base gravity pull strength for black holes (scales with proximity)")
    public static double blackHoleGravityStrength = 0.15;

    @ConfigValue(name = "Black Hole Damage Per Tick", comment = "Base damage per tick from black holes (scales with proximity)")
    public static float blackHoleDamagePerTick = 1.0f;
}
