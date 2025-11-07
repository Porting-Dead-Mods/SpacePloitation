package com.portingdeadmods.spaceploitation.content.blockentity;

import com.portingdeadmods.portingdeadlibs.utils.capabilities.HandlerUtils;
import com.portingdeadmods.spaceploitation.registries.MJBlockEntities;
import com.portingdeadmods.portingdeadlibs.api.blockentities.ContainerBlockEntity;
import com.portingdeadmods.portingdeadlibs.api.utils.IOAction;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CreativePowerBlockEntity extends ContainerBlockEntity {
    public CreativePowerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(MJBlockEntities.CREATIVE_POWER.get(), blockPos, blockState);
        addEnergyStorage(HandlerUtils::newEnergystorage, builder -> builder
                .capacity(Integer.MAX_VALUE)
                .onChange(this::updateData)
                .maxTransfer(Integer.MAX_VALUE));
    }

    @Override
    public void tick() {
        if (level.isClientSide) return;
        
        getEnergyStorage().receiveEnergy(Integer.MAX_VALUE, false);
        
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(direction);
            IEnergyStorage neighborEnergy = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, direction.getOpposite());
            
            if (neighborEnergy != null && neighborEnergy.canReceive()) {
                neighborEnergy.receiveEnergy(Integer.MAX_VALUE, false);
            }
        }
    }

}
