package com.portingdeadmods.spaceploitation.content.blockentity;

import com.portingdeadmods.portingdeadlibs.utils.capabilities.HandlerUtils;
import com.portingdeadmods.spaceploitation.Spaceploitation;
import com.portingdeadmods.spaceploitation.capabilities.ReadOnlyEnergyStorage;
import com.portingdeadmods.spaceploitation.capabilities.ReadOnlyFluidHandler;
import com.portingdeadmods.spaceploitation.capabilities.ReadOnlyItemHandler;
import com.portingdeadmods.spaceploitation.capabilities.UpgradeItemHandler;
import com.portingdeadmods.spaceploitation.content.block.UpgradeBlockEntity;
import com.portingdeadmods.spaceploitation.utils.NumberFormatUtils;
import guideme.internal.shaded.lucene.codecs.compressing.Compressor;
import io.netty.buffer.Unpooled;
import com.portingdeadmods.spaceploitation.content.items.UpgradeItem;
import com.portingdeadmods.spaceploitation.content.blockentity.bus.AbstractBusBlockEntity;
import com.portingdeadmods.spaceploitation.content.blockentity.bus.EnergyInputBusBlockEntity;
import com.portingdeadmods.spaceploitation.content.blockentity.bus.EnergyOutputBusBlockEntity;
import com.portingdeadmods.spaceploitation.content.blockentity.bus.FluidInputBusBlockEntity;
import com.portingdeadmods.spaceploitation.content.blockentity.bus.FluidOutputBusBlockEntity;
import com.portingdeadmods.spaceploitation.content.blockentity.bus.ItemInputBusBlockEntity;
import com.portingdeadmods.spaceploitation.content.blockentity.bus.ItemOutputBusBlockEntity;
import com.portingdeadmods.spaceploitation.content.menus.PlanetSimulatorMenu;
import com.portingdeadmods.spaceploitation.content.recipe.PlanetPowerRecipe;
import com.portingdeadmods.spaceploitation.content.recipe.PlanetSimulatorRecipe;
import com.portingdeadmods.spaceploitation.data.BusType;
import com.portingdeadmods.spaceploitation.data.PlanetComponent;
import com.portingdeadmods.spaceploitation.data.UpgradeType;
import com.portingdeadmods.spaceploitation.registries.*;
import com.portingdeadmods.portingdeadlibs.api.blockentities.ContainerBlockEntity;
import com.portingdeadmods.portingdeadlibs.api.blockentities.multiblocks.MultiblockEntity;
import com.portingdeadmods.portingdeadlibs.api.multiblocks.Multiblock;
import com.portingdeadmods.portingdeadlibs.api.multiblocks.MultiblockData;
import com.portingdeadmods.portingdeadlibs.api.multiblocks.MultiblockLayer;
import com.portingdeadmods.portingdeadlibs.api.recipes.IngredientWithCount;
import com.portingdeadmods.portingdeadlibs.api.utils.HorizontalDirection;
import com.portingdeadmods.portingdeadlibs.api.utils.IOAction;
import com.portingdeadmods.portingdeadlibs.utils.MultiblockHelper;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.portingdeadmods.spaceploitation.content.blockentity.CompressorBlockEntity.UPGRADES_ID;

public class PlanetSimulatorBlockEntity extends ContainerBlockEntity implements MultiblockEntity, MenuProvider, UpgradeBlockEntity {
    public static final Set<ResourceKey<UpgradeType>> SUPPORTED_UPGRADES = Set.of(
            ResourceKey.create(MJRegistries.UPGRADE_TYPE_KEY, Spaceploitation.rl("speed")),
            ResourceKey.create(MJRegistries.UPGRADE_TYPE_KEY, Spaceploitation.rl("energy")),
            ResourceKey.create(MJRegistries.UPGRADE_TYPE_KEY, Spaceploitation.rl("luck"))
    );
    private MultiblockData multiblockData;

    private int progress = 0;
    private int maxProgress = 0;
    private int energyPerTick = 0;
    private boolean isProcessing = false;
    private PlanetSimulatorRecipe currentRegularRecipe = null;
    @Nullable
    private ResourceLocation currentRegularRecipeId = null;
    private PlanetPowerRecipe currentPowerRecipe = null;
    @Nullable
    private ResourceLocation currentPowerRecipeId = null;
    private boolean isRegularRecipe = false;
    private List<PlanetSimulatorRecipe.WeightedOutput> clientOutputs = List.of();
    
    private int clientTotalEnergy = 0;
    private int clientMaxEnergy = 0;
    
    public int getProgress() {
        return progress;
    }
    
    public int getMaxProgress() {
        return maxProgress;
    }
    
    public int getEnergyPerTick() {
        return energyPerTick;
    }
    
    public boolean isProcessing() {
        return isProcessing;
    }

    public List<AbstractBusBlockEntity> getInputBusses() {
        return getBusses(true);
    }

    public List<AbstractBusBlockEntity> getOutputBusses() {
        return getBusses(false);
    }

    public int getTotalInputEnergy() {
        return getTotalInputEnergy(getInputBusses());
    }

    public int getTotalMaxInputEnergy() {
        int total = 0;
        for (AbstractBusBlockEntity bus : getInputBusses()) {
            if (bus.getBusType() == BusType.ENERGY && bus instanceof EnergyInputBusBlockEntity energyBus) {
                total += energyBus.getEnergyStorage().getMaxEnergyStored();
            }
        }
        return total;
    }

    public Map<Item, Integer> getAllInputItems() {
        return gatherAllInputItems(getInputBusses());
    }

    public FluidStack getAllInputFluids() {
        return gatherAllInputFluids(getInputBusses());
    }

    public Map<Item, Integer> getAllOutputItems() {
        Map<Item, Integer> itemMap = new HashMap<>();
        for (AbstractBusBlockEntity bus : getOutputBusses()) {
            if (bus.getBusType() == BusType.ITEM && bus instanceof ItemOutputBusBlockEntity itemBus) {
                net.neoforged.neoforge.items.ItemStackHandler handler = itemBus.getItemHandler();
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        itemMap.merge(stack.getItem(), stack.getCount(), Integer::sum);
                    }
                }
            }
        }
        return itemMap;
    }

    public FluidStack getAllOutputFluids() {
        for (AbstractBusBlockEntity bus : getOutputBusses()) {
            if (bus.getBusType() == BusType.FLUID && bus instanceof FluidOutputBusBlockEntity fluidBus) {
                FluidStack fluid = fluidBus.getFluidTank().getFluid();
                if (!fluid.isEmpty()) {
                    return fluid;
                }
            }
        }
        return FluidStack.EMPTY;
    }

    public PlanetSimulatorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(MJBlockEntities.PLANET_SIMULATOR.get(), blockPos, blockState);
        this.addItemHandler(HandlerUtils::newItemStackHandler, builder -> builder
                .slots(1)
                .slotLimit($ -> 1)
                .onChange($ -> this.updateData())
                .validator((slot, item) -> item.has(MJDataComponents.PLANET)));

        addHandler(UPGRADES_ID, new UpgradeItemHandler(SUPPORTED_UPGRADES) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);

                updateData();
            }

            @Override
            public void onUpgradeAdded(ResourceKey<UpgradeType> upgrade) {
                super.onUpgradeAdded(upgrade);

                PlanetSimulatorBlockEntity.this.onUpgradeAdded(upgrade);
            }

            @Override
            public void onUpgradeRemoved(ResourceKey<UpgradeType> upgrade) {
                super.onUpgradeRemoved(upgrade);

                PlanetSimulatorBlockEntity.this.onUpgradeRemoved(upgrade);
            }
        });

    }

    @Override
    public void tick() {
        if (this.level.getGameTime() % 20 == 0 && !this.getBlockState().getValue(Multiblock.FORMED)) {
            MJMultiblocks.PLANET_SIMULATOR.get().form(this.level, this.worldPosition);
        }

        if (this.getBlockState().getValue(Multiblock.FORMED)) {
            processRecipes();
        }
        
    }
    
    public String getDisplayText() {
        StringBuilder info = new StringBuilder();
        
        int energyStored = this.level != null && this.level.isClientSide ? clientTotalEnergy : getTotalInputEnergy(getInputBusses());
        int maxEnergy = this.level != null && this.level.isClientSide ? clientMaxEnergy : getTotalMaxInputEnergy();
        info.append("Energy: ").append(NumberFormatUtils.formatEnergy(energyStored))
            .append(" / ").append(NumberFormatUtils.formatEnergy(maxEnergy)).append(" FE\n");
        
        if (!isProcessing) {
            return info.toString();
        }
        
        if (isRegularRecipe) {
            info.append("\nRecipe: Planet Simulation");
        } else {
            info.append("\nRecipe: Power Generation");
        }
        
        info.append("\nEnergy/t: ");
        if (!isRegularRecipe) {
            info.append("+");
        }
        info.append(NumberFormatUtils.formatEnergy(energyPerTick)).append(" FE/t");
        
        int timeLeft = (maxProgress - progress) / 20;
        info.append("\nTime Left: ").append(timeLeft).append("s");
        info.append("\nProgress: ").append(progress).append("/").append(maxProgress);
        
        if (!clientOutputs.isEmpty() && isRegularRecipe) {
            info.append("\n\nOutputs:");
            for (PlanetSimulatorRecipe.WeightedOutput output : clientOutputs) {
                float chance = applyLuckUpgrade(output.chance());
                info.append("\n  ");
                if (output.itemStack().isPresent()) {
                    info.append(output.itemStack().get().getHoverName().getString());
                } else if (output.fluidStack().isPresent()) {
                    info.append(output.fluidStack().get().getHoverName().getString());
                }
                info.append(" (").append(String.format("%.1f%%", chance * 100)).append(")");
            }
        }
        
        return info.toString();
    }

    private void processRecipes() {
        if (this.level == null || this.level.isClientSide) return;
        this.updateData();

        ItemStack planetCard = this.getItemHandler().getStackInSlot(0);
        if (planetCard.isEmpty() || !planetCard.has(MJDataComponents.PLANET)) {
            resetProgress();
            return;
        }

        PlanetComponent planetComponent = planetCard.get(MJDataComponents.PLANET);
        if (planetComponent == null) {
            resetProgress();
            return;
        }

        List<AbstractBusBlockEntity> inputBusses = getBusses(true);
        List<AbstractBusBlockEntity> outputBusses = getBusses(false);

        if (isProcessing) {
            boolean resolved = true;
            if (isRegularRecipe) {
                if (currentRegularRecipe == null && currentRegularRecipeId != null) {
                    resolved = resolveRegularRecipe();
                } else if (currentRegularRecipe == null) {
                    resolved = false;
                }
            } else {
                if (currentPowerRecipe == null && currentPowerRecipeId != null) {
                    resolved = resolvePowerRecipe();
                } else if (currentPowerRecipe == null) {
                    resolved = false;
                }
            }

            if (!resolved) {
                resetProgress();
                return;
            }
        }

        if (isProcessing && !isRegularRecipe && currentPowerRecipe != null && currentPowerRecipeId != null) {
            processPowerRecipe(currentPowerRecipe, currentPowerRecipeId, inputBusses);
            return;
        }

        if (isProcessing && isRegularRecipe && currentRegularRecipe != null && currentRegularRecipeId != null) {
            processRegularRecipe(currentRegularRecipe, currentRegularRecipeId, inputBusses, outputBusses);
            return;
        }

        RecipeHolder<PlanetSimulatorRecipe> regularRecipe = findMatchingRegularRecipe(planetComponent, inputBusses);
        RecipeHolder<PlanetPowerRecipe> powerRecipe = findMatchingPowerRecipe(planetComponent, inputBusses);

        if (regularRecipe != null) {
            processRegularRecipe(regularRecipe.value(), regularRecipe.id(), inputBusses, outputBusses);
        } else if (powerRecipe != null) {
            processPowerRecipe(powerRecipe.value(), powerRecipe.id(), inputBusses);
        } else {
            resetProgress();
        }
    }

    private void resetProgress() {
        progress = 0;
        maxProgress = 0;
        energyPerTick = 0;
        isProcessing = false;
        currentRegularRecipe = null;
        currentRegularRecipeId = null;
        currentPowerRecipe = null;
        currentPowerRecipeId = null;
        isRegularRecipe = false;
        clientOutputs = List.of();
    }

    private List<AbstractBusBlockEntity> getBusses(boolean input) {
        List<AbstractBusBlockEntity> busses = new ArrayList<>();
        if (this.multiblockData == null || this.level == null) return busses;

        MultiblockLayer[] layers = this.multiblockData.layers();
        if (layers == null) return busses;

        for (MultiblockLayer layer : layers) {
            for (int i = 0; i < layer.layer().length; i++) {
                BlockPos pos = getBlockPosFromLayer(layer, i);
                if (pos != null) {
                    BlockEntity be = this.level.getBlockEntity(pos);
                    if (be instanceof AbstractBusBlockEntity bus && bus.isInput() == input) {
                        busses.add(bus);
                    }
                }
            }
        }
        return busses;
    }

    private BlockPos getBlockPosFromLayer(MultiblockLayer layer, int index) {
        if (this.multiblockData == null) return null;
        
        HorizontalDirection direction = this.multiblockData.direction();
        if (direction == null) return null;

        int width = layer.widths().leftInt();
        int x = index % width;
        int z = index / width;
        int y = 0;

        for (int i = 0; i < this.multiblockData.layers().length; i++) {
            if (this.multiblockData.layers()[i] == layer) {
                y = i;
                break;
            }
        }

        Vec3i relativeControllerPos = MultiblockHelper.getRelativeControllerPos(MJMultiblocks.PLANET_SIMULATOR.get());
        BlockPos firstBlockPos = MultiblockHelper.getFirstBlockPos(direction, this.worldPosition, relativeControllerPos);
        
        return MultiblockHelper.getCurPos(firstBlockPos, new Vec3i(x, y, z), direction);
    }

    private RecipeHolder<PlanetSimulatorRecipe> findMatchingRegularRecipe(PlanetComponent planetComponent, List<AbstractBusBlockEntity> inputBusses) {
        if (this.level == null || planetComponent.planetType().isEmpty()) return null;

        var planetTypeRegistry = this.level.registryAccess().lookupOrThrow(MJRegistries.PLANET_TYPE_KEY);
        var planetTypeKey = planetTypeRegistry.listElements()
                .filter(holder -> holder.value().equals(planetComponent.planetType().get()))
                .map(Holder.Reference::key)
                .findFirst()
                .orElse(null);

        if (planetTypeKey == null) return null;

        return this.level.getRecipeManager()
                .getAllRecipesFor(PlanetSimulatorRecipe.TYPE)
                .stream()
                .filter(recipe -> recipe.value().planetType().equals(planetTypeKey))
                .filter(recipe -> hasRequiredInputs(recipe.value(), inputBusses))
                .sorted((r1, r2) -> Integer.compare(getRecipeSpecificity(r2.value(), inputBusses), getRecipeSpecificity(r1.value(), inputBusses)))
                .findFirst()
                .orElse(null);
    }

    private RecipeHolder<PlanetPowerRecipe> findMatchingPowerRecipe(PlanetComponent planetComponent, List<AbstractBusBlockEntity> inputBusses) {
        if (this.level == null || planetComponent.planetType().isEmpty()) return null;

        var planetTypeRegistry = this.level.registryAccess().lookupOrThrow(MJRegistries.PLANET_TYPE_KEY);
        var planetTypeKey = planetTypeRegistry.listElements()
                .filter(holder -> holder.value().equals(planetComponent.planetType().get()))
                .map(Holder.Reference::key)
                .findFirst()
                .orElse(null);

        if (planetTypeKey == null) return null;

        return this.level.getRecipeManager()
                .getAllRecipesFor(PlanetPowerRecipe.TYPE)
                .stream()
                .filter(recipe -> recipe.value().planetType().equals(planetTypeKey))
                .filter(recipe -> hasRequiredInputs(recipe.value(), inputBusses))
                .sorted((r1, r2) -> Integer.compare(getRecipeSpecificity(r2.value(), inputBusses), getRecipeSpecificity(r1.value(), inputBusses)))
                .findFirst()
                .orElse(null);
    }

    private int getRecipeSpecificity(PlanetSimulatorRecipe recipe, List<AbstractBusBlockEntity> inputBusses) {
        int specificity = 0;
        
        // Catalysts are required but not consumed - give them medium weight
        specificity += recipe.catalysts().size() * 100;
        for (IngredientWithCount catalyst : recipe.catalysts()) {
            specificity += catalyst.count();
        }
        
        // Inputs are consumed - give them higher weight since they're more restrictive
        specificity += recipe.inputs().size() * 200;
        for (IngredientWithCount input : recipe.inputs()) {
            specificity += input.count() * 2;
        }
        
        if (recipe.fluidInput().isPresent()) {
            specificity += 1000;
            for (FluidStack fluid : recipe.fluidInput().get().getStacks()) {
                specificity += fluid.getAmount();
            }
        }
        
        return specificity;
    }

    private int getRecipeSpecificity(PlanetPowerRecipe recipe, List<AbstractBusBlockEntity> inputBusses) {
        int specificity = 0;
        
        // Catalysts are required but not consumed - give them medium weight
        specificity += recipe.catalysts().size() * 100;
        for (IngredientWithCount catalyst : recipe.catalysts()) {
            specificity += catalyst.count();
        }
        
        // Inputs are consumed - give them higher weight since they're more restrictive
        specificity += recipe.inputs().size() * 200;
        for (IngredientWithCount input : recipe.inputs()) {
            specificity += input.count() * 2;
        }
        
        if (recipe.fluidInput().isPresent()) {
            specificity += 1000;
            for (FluidStack fluid : recipe.fluidInput().get().getStacks()) {
                specificity += fluid.getAmount();
            }
        }
        
        return specificity;
    }

    private boolean hasRequiredInputs(PlanetSimulatorRecipe recipe, List<AbstractBusBlockEntity> inputBusses) {
        Map<Item, Integer> availableItems = gatherAllInputItems(inputBusses);
        FluidStack availableFluid = gatherAllInputFluids(inputBusses);

        // Check catalysts (required but not consumed)
        for (IngredientWithCount catalyst : recipe.catalysts()) {
            int totalFound = 0;
            for (Map.Entry<Item, Integer> entry : availableItems.entrySet()) {
                if (catalyst.ingredient().test(new ItemStack(entry.getKey()))) {
                    totalFound += entry.getValue();
                }
            }
            if (totalFound < catalyst.count()) return false;
        }

        // Check inputs (required and consumed)
        for (IngredientWithCount input : recipe.inputs()) {
            int totalFound = 0;
            for (Map.Entry<Item, Integer> entry : availableItems.entrySet()) {
                if (input.ingredient().test(new ItemStack(entry.getKey()))) {
                    totalFound += entry.getValue();
                }
            }
            if (totalFound < input.count()) return false;
        }

        if (recipe.fluidInput().isPresent()) {
            if (availableFluid.isEmpty() || !recipe.fluidInput().get().test(availableFluid)) {
                return false;
            }
        }

        return true;
    }

    private boolean hasRequiredInputs(PlanetPowerRecipe recipe, List<AbstractBusBlockEntity> inputBusses) {
        Map<Item, Integer> availableItems = gatherAllInputItems(inputBusses);
        FluidStack availableFluid = gatherAllInputFluids(inputBusses);

        // Check catalysts (required but not consumed)
        for (IngredientWithCount catalyst : recipe.catalysts()) {
            int totalFound = 0;
            for (Map.Entry<Item, Integer> entry : availableItems.entrySet()) {
                if (catalyst.ingredient().test(new ItemStack(entry.getKey()))) {
                    totalFound += entry.getValue();
                }
            }
            if (totalFound < catalyst.count()) return false;
        }

        // Check inputs (required and consumed)
        for (IngredientWithCount input : recipe.inputs()) {
            int totalFound = 0;
            for (Map.Entry<Item, Integer> entry : availableItems.entrySet()) {
                if (input.ingredient().test(new ItemStack(entry.getKey()))) {
                    totalFound += entry.getValue();
                }
            }
            if (totalFound < input.count()) return false;
        }

        if (recipe.fluidInput().isPresent()) {
            if (availableFluid.isEmpty() || !recipe.fluidInput().get().test(availableFluid)) {
                return false;
            }
        }

        return true;
    }

    private Map<Item, Integer> gatherAllInputItems(List<AbstractBusBlockEntity> inputBusses) {
        Map<Item, Integer> itemMap = new HashMap<>();
        for (AbstractBusBlockEntity bus : inputBusses) {
            if (bus.getBusType() == BusType.ITEM && bus instanceof ItemInputBusBlockEntity itemBus) {
                IItemHandler handler = itemBus.getItemHandler();
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        itemMap.merge(stack.getItem(), stack.getCount(), Integer::sum);
                    }
                }
            }
        }
        return itemMap;
    }

    private FluidStack gatherAllInputFluids(List<AbstractBusBlockEntity> inputBusses) {
        for (AbstractBusBlockEntity bus : inputBusses) {
            if (bus.getBusType() == BusType.FLUID && bus instanceof FluidInputBusBlockEntity fluidBus) {
                FluidStack fluid = fluidBus.getFluidTank().getFluid();
                if (!fluid.isEmpty()) {
                    return fluid;
                }
            }
        }
        return FluidStack.EMPTY;
    }

    private int getTotalInputEnergy(List<AbstractBusBlockEntity> inputBusses) {
        int total = 0;
        for (AbstractBusBlockEntity bus : inputBusses) {
            if (bus.getBusType() == BusType.ENERGY && bus instanceof EnergyInputBusBlockEntity energyBus) {
                total += energyBus.getEnergyStorage().getEnergyStored();
            }
        }
        return total;
    }

    private boolean resolveRegularRecipe() {
        if (this.level == null || currentRegularRecipeId == null) return false;

        Optional<RecipeHolder<PlanetSimulatorRecipe>> recipeHolder = this.level.getRecipeManager()
                .getAllRecipesFor(PlanetSimulatorRecipe.TYPE)
                .stream()
                .filter(holder -> holder.id().equals(currentRegularRecipeId))
                .findFirst();

        if (recipeHolder.isEmpty()) {
            Spaceploitation.LOGGER.warn("Failed to resolve regular recipe {} for Planet Simulator at {}", currentRegularRecipeId, this.worldPosition);
            return false;
        }

        currentRegularRecipe = recipeHolder.get().value();
        clientOutputs = currentRegularRecipe.outputs();
        return true;
    }

    private boolean resolvePowerRecipe() {
        if (this.level == null || currentPowerRecipeId == null) return false;

        Optional<RecipeHolder<PlanetPowerRecipe>> recipeHolder = this.level.getRecipeManager()
                .getAllRecipesFor(PlanetPowerRecipe.TYPE)
                .stream()
                .filter(holder -> holder.id().equals(currentPowerRecipeId))
                .findFirst();

        if (recipeHolder.isEmpty()) {
            Spaceploitation.LOGGER.warn("Failed to resolve power recipe {} for Planet Simulator at {}", currentPowerRecipeId, this.worldPosition);
            return false;
        }

        currentPowerRecipe = recipeHolder.get().value();
        return true;
    }

    private void processRegularRecipe(PlanetSimulatorRecipe recipe, ResourceLocation recipeId, List<AbstractBusBlockEntity> inputBusses, List<AbstractBusBlockEntity> outputBusses) {
        if (!isProcessing) {
            isProcessing = true;
            currentRegularRecipe = recipe;
            currentRegularRecipeId = recipeId;
            currentPowerRecipe = null;
            currentPowerRecipeId = null;
            isRegularRecipe = true;
            clientOutputs = recipe.outputs();
            applyUpgrades(recipe.duration(), recipe.energyPerTick());
            progress = 0;
        }

        int availableEnergy = getTotalInputEnergy(inputBusses);
        if (availableEnergy < energyPerTick) {
            return;
        }

        consumeEnergy(inputBusses, energyPerTick);
        progress++;
        this.setChanged();

        if (progress >= maxProgress) {
            if (consumeInputs(recipe, inputBusses) && canOutputResults(recipe.outputs(), outputBusses)) {
                outputResults(recipe.outputs(), outputBusses);
                resetProgress();
            } else {
                progress = maxProgress - 1;
            }
        }
    }

    private int applyUpgrades(int baseDuration, int baseEnergyPerTick) {
        float duration = baseDuration;
        float energyPerTick = baseEnergyPerTick;
        
        for (ResourceKey<UpgradeType> upgradeTypeKey : SUPPORTED_UPGRADES) {
            int count = getUpgradeAmount(upgradeTypeKey);
            if (count == 0) continue;
            
            UpgradeType upgradeType = level.registryAccess()
                    .lookupOrThrow(MJRegistries.UPGRADE_TYPE_KEY)
                    .get(upgradeTypeKey)
                    .map(holder -> holder.value())
                    .orElse(new UpgradeType(List.of()));
            
            for (UpgradeType.UpgradeEffect effect : upgradeType.getEffects()) {
                switch (effect.getTarget()) {
                    case DURATION -> duration = effect.apply(duration, count);
                    case ENERGY_USAGE -> energyPerTick = effect.apply(energyPerTick, count);
                }
            }
        }
        
        this.maxProgress = Math.max(1, (int) duration);
        this.energyPerTick = Math.max(1, (int) energyPerTick);
        
        return this.maxProgress;
    }

    private float applyLuckUpgrade(float baseChance) {
        if (baseChance >= 1.0f) return baseChance;
        
        float totalBonus = 0.0f;
        
        for (ResourceKey<UpgradeType> upgradeTypeKey : SUPPORTED_UPGRADES) {
            int count = getUpgradeAmount(upgradeTypeKey);
            if (count == 0) continue;
            
            UpgradeType upgradeType = level.registryAccess()
                    .lookupOrThrow(MJRegistries.UPGRADE_TYPE_KEY)
                    .get(upgradeTypeKey)
                    .map(holder -> holder.value())
                    .orElse(new UpgradeType(List.of()));
            
            for (UpgradeType.UpgradeEffect effect : upgradeType.getEffects()) {
                if (effect.getTarget() == UpgradeType.EffectTarget.LUCK_CHANCE) {
                    totalBonus += effect.getPercentPerUpgrade() * count;
                }
            }
        }
        
        return Math.min(1.0f, baseChance * (1.0f + totalBonus / 100.0f));
    }

    private void processPowerRecipe(PlanetPowerRecipe recipe, ResourceLocation recipeId, List<AbstractBusBlockEntity> inputBusses) {
        if (!isProcessing) {
            Spaceploitation.LOGGER.info("Starting power recipe. Checking inputs...");
            if (!hasRequiredInputs(recipe, inputBusses)) {
                Spaceploitation.LOGGER.info("Not enough inputs available");
                return;
            }
            Spaceploitation.LOGGER.info("Inputs available. Starting recipe...");
            isProcessing = true;
            currentRegularRecipe = null;
            currentRegularRecipeId = null;
            currentPowerRecipe = recipe;
            currentPowerRecipeId = recipeId;
            isRegularRecipe = false;
            applyUpgrades(recipe.duration(), recipe.energyPerTick());
            progress = 0;
            Spaceploitation.LOGGER.info("Recipe setup complete. maxProgress={}, energyPerTick={}", maxProgress, energyPerTick);
            Spaceploitation.LOGGER.info("Consuming inputs...");
            if (!consumeInputs(recipe, inputBusses)) {
                Spaceploitation.LOGGER.error("Failed to consume inputs!");
                resetProgress();
                return;
            }
            Spaceploitation.LOGGER.info("Inputs consumed successfully");
        }

        List<AbstractBusBlockEntity> outputBusses = getOutputBusses();
        if (!canGenerateEnergy(outputBusses, energyPerTick)) {
            if (Spaceploitation.LOGGER.isDebugEnabled()) {
                Spaceploitation.LOGGER.debug("Skipping power generation tick for Planet Simulator at {} - unable to output {} FE", this.worldPosition, energyPerTick);
            }
            return;
        }

        int generated = generateEnergy(outputBusses, energyPerTick);
        if (generated <= 0) {
            Spaceploitation.LOGGER.debug("Failed to push any energy despite reported capacity for Planet Simulator at {}", this.worldPosition);
            return;
        }
        if (Spaceploitation.LOGGER.isDebugEnabled()) {
            Spaceploitation.LOGGER.debug("Generated {} FE (requested {}) this tick for Planet Simulator at {}", generated, energyPerTick, this.worldPosition);
        }
        progress++;
        this.setChanged();

        if (progress >= maxProgress) {
            Spaceploitation.LOGGER.info("Power recipe completed. Resetting...");
            resetProgress();
        }
    }

    private boolean consumeInputs(PlanetSimulatorRecipe recipe, List<AbstractBusBlockEntity> inputBusses) {
        // DO NOT consume catalysts - they are required but not consumed
        // Only consume inputs
        
        for (IngredientWithCount input : recipe.inputs()) {
            int remaining = input.count();
            for (AbstractBusBlockEntity bus : inputBusses) {
                if (bus.getBusType() == BusType.ITEM && bus instanceof ItemInputBusBlockEntity itemBus) {
                    IItemHandler handler = itemBus.getItemHandler();
                    for (int i = 0; i < handler.getSlots() && remaining > 0; i++) {
                        ItemStack stack = handler.getStackInSlot(i);
                        if (input.ingredient().test(stack)) {
                            int toExtract = Math.min(remaining, stack.getCount());
                            handler.extractItem(i, toExtract, false);
                            remaining -= toExtract;
                        }
                    }
                }
            }
            if (remaining > 0) return false;
        }

        if (recipe.fluidInput().isPresent()) {
            for (AbstractBusBlockEntity bus : inputBusses) {
                if (bus.getBusType() == BusType.FLUID && bus instanceof FluidInputBusBlockEntity fluidBus) {
                    FluidStack fluid = fluidBus.getFluidTank().getFluid();
                    if (recipe.fluidInput().get().test(fluid)) {
                        int amountToDrain = fluid.getAmount();
                        for (FluidStack ingredientFluid : recipe.fluidInput().get().getStacks()) {
                            if (ingredientFluid.getFluid().isSame(fluid.getFluid())) {
                                amountToDrain = Math.min(ingredientFluid.getAmount(), fluid.getAmount());
                                break;
                            }
                        }
                        fluidBus.getFluidTank().drain(amountToDrain, IFluidHandler.FluidAction.EXECUTE);
                        return true;
                    }
                }
            }
            return false;
        }

        return true;
    }

    private boolean consumeInputs(PlanetPowerRecipe recipe, List<AbstractBusBlockEntity> inputBusses) {
        // DO NOT consume catalysts - they are required but not consumed
        // Only consume inputs
        
        for (IngredientWithCount input : recipe.inputs()) {
            int remaining = input.count();
            for (AbstractBusBlockEntity bus : inputBusses) {
                if (bus.getBusType() == BusType.ITEM && bus instanceof ItemInputBusBlockEntity itemBus) {
                    IItemHandler handler = itemBus.getItemHandler();
                    for (int i = 0; i < handler.getSlots() && remaining > 0; i++) {
                        ItemStack stack = handler.getStackInSlot(i);
                        if (input.ingredient().test(stack)) {
                            int toExtract = Math.min(remaining, stack.getCount());
                            handler.extractItem(i, toExtract, false);
                            remaining -= toExtract;
                        }
                    }
                }
            }
            if (remaining > 0) return false;
        }

        if (recipe.fluidInput().isPresent()) {
            for (AbstractBusBlockEntity bus : inputBusses) {
                if (bus.getBusType() == BusType.FLUID && bus instanceof FluidInputBusBlockEntity fluidBus) {
                    FluidStack fluid = fluidBus.getFluidTank().getFluid();
                    if (recipe.fluidInput().get().test(fluid)) {
                        int amountToDrain = fluid.getAmount();
                        for (FluidStack ingredientFluid : recipe.fluidInput().get().getStacks()) {
                            if (ingredientFluid.getFluid().isSame(fluid.getFluid())) {
                                amountToDrain = Math.min(ingredientFluid.getAmount(), fluid.getAmount());
                                break;
                            }
                        }
                        fluidBus.getFluidTank().drain(amountToDrain, IFluidHandler.FluidAction.EXECUTE);
                        return true;
                    }
                }
            }
            return false;
        }

        return true;
    }

    private void consumeEnergy(List<AbstractBusBlockEntity> inputBusses, int amount) {
        int remaining = amount;
        for (AbstractBusBlockEntity bus : inputBusses) {
            if (bus.getBusType() == BusType.ENERGY && bus instanceof EnergyInputBusBlockEntity energyBus && remaining > 0) {
                int extracted = energyBus.getEnergyStorage().extractEnergy(remaining, false);
                remaining -= extracted;
            }
        }
    }

    private boolean canGenerateEnergy(List<AbstractBusBlockEntity> outputBusses, int amount) {
        boolean hasEnergyBus = false;
        for (AbstractBusBlockEntity bus : outputBusses) {
            if (bus.getBusType() == BusType.ENERGY && bus instanceof EnergyOutputBusBlockEntity energyBus) {
                hasEnergyBus = true;
                int current = energyBus.getEnergyStorage().getEnergyStored();
                int max = energyBus.getEnergyStorage().getMaxEnergyStored();
                if (current < max) {
                    return true;
                }
            }
        }
        if (Spaceploitation.LOGGER.isDebugEnabled()) {
            if (!hasEnergyBus) {
                Spaceploitation.LOGGER.debug("Planet Simulator at {} found no energy output buses while trying to output {} FE/t", this.worldPosition, amount);
            } else {
                Spaceploitation.LOGGER.debug("Planet Simulator at {} could not output {} FE/t because all energy output buses are full", this.worldPosition, amount);
            }
        }
        return false;
    }

    private int generateEnergy(List<AbstractBusBlockEntity> outputBusses, int amount) {
        int remaining = amount;
        if (Spaceploitation.LOGGER.isDebugEnabled()) {
            Spaceploitation.LOGGER.debug("Planet Simulator at {} attempting to distribute {} FE across {} output buses", this.worldPosition, amount, outputBusses.size());
        }
        for (AbstractBusBlockEntity bus : outputBusses) {
            if (bus.getBusType() == BusType.ENERGY && bus instanceof EnergyOutputBusBlockEntity energyBus && remaining > 0) {
                int current = energyBus.getEnergyStorage().getEnergyStored();
                int max = energyBus.getEnergyStorage().getMaxEnergyStored();
                int capacity = max - current;
                if (capacity <= 0) {
                    if (Spaceploitation.LOGGER.isDebugEnabled()) {
                        Spaceploitation.LOGGER.debug("Energy output bus at {} is full ({}/{})", bus.getBlockPos(), current, max);
                    }
                    continue;
                }
                int canAdd = Math.min(remaining, capacity);
                int inserted = energyBus.addEnergy(canAdd);
                if (Spaceploitation.LOGGER.isDebugEnabled()) {
                    if (inserted > 0) {
                        int stored = energyBus.getEnergyStorage().getEnergyStored();
                        Spaceploitation.LOGGER.debug("Inserted {} FE into energy output bus at {} (now {}/{})", inserted, bus.getBlockPos(), stored, max);
                    } else {
                        Spaceploitation.LOGGER.debug("Energy output bus at {} rejected {} FE (current {}/{})", bus.getBlockPos(), canAdd, current, max);
                    }
                }
                remaining -= inserted;
                if (remaining <= 0) {
                    break;
                }
            }
        }
        if (remaining > 0 && Spaceploitation.LOGGER.isDebugEnabled()) {
            Spaceploitation.LOGGER.debug("Planet Simulator at {} still had {} FE remaining after distribution attempt", this.worldPosition, remaining);
        }
        return amount - remaining;
    }

    private boolean canOutputResults(List<PlanetSimulatorRecipe.WeightedOutput> outputs, List<AbstractBusBlockEntity> outputBusses) {
        for (PlanetSimulatorRecipe.WeightedOutput output : outputs) {
            float chance = applyLuckUpgrade(output.chance());
            if (Math.random() > chance) continue;

            if (output.itemStack().isPresent()) {
                ItemStack stack = output.itemStack().get();
                if (!canInsertItem(stack, outputBusses)) {
                    return false;
                }
            }

            if (output.fluidStack().isPresent()) {
                FluidStack stack = output.fluidStack().get();
                if (!canInsertFluid(stack, outputBusses)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean canInsertItem(ItemStack stack, List<AbstractBusBlockEntity> outputBusses) {
        List<ItemOutputBusBlockEntity> itemBusses = outputBusses.stream()
                .filter(bus -> bus.getBusType() == BusType.ITEM)
                .map(bus -> (ItemOutputBusBlockEntity) bus)
                .collect(Collectors.toList());

        for (ItemOutputBusBlockEntity bus : itemBusses) {
            IItemHandler handler = bus.getItemHandler();
            ItemStack remaining = stack.copy();
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack existing = handler.getStackInSlot(i);
                if (existing.isEmpty()) {
                    return true;
                } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                    int maxStack = Math.min(handler.getSlotLimit(i), stack.getMaxStackSize());
                    if (existing.getCount() < maxStack) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean canInsertFluid(FluidStack stack, List<AbstractBusBlockEntity> outputBusses) {
        List<FluidOutputBusBlockEntity> fluidBusses = outputBusses.stream()
                .filter(bus -> bus.getBusType() == BusType.FLUID)
                .map(bus -> (FluidOutputBusBlockEntity) bus)
                .collect(Collectors.toList());

        for (FluidOutputBusBlockEntity bus : fluidBusses) {
            FluidStack existing = bus.getFluidTank().getFluid();
            if (existing.isEmpty()) {
                return true;
            } else if (existing.isFluidEqual(stack)) {
                if (existing.getAmount() + stack.getAmount() <= bus.getFluidTank().getCapacity()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void outputResults(List<PlanetSimulatorRecipe.WeightedOutput> outputs, List<AbstractBusBlockEntity> outputBusses) {
        for (PlanetSimulatorRecipe.WeightedOutput output : outputs) {
            float chance = applyLuckUpgrade(output.chance());
            if (Math.random() > chance) continue;

            if (output.itemStack().isPresent()) {
                insertItem(output.itemStack().get(), outputBusses);
            }

            if (output.fluidStack().isPresent()) {
                insertFluid(output.fluidStack().get(), outputBusses);
            }
        }
    }

    private void insertItem(ItemStack stack, List<AbstractBusBlockEntity> outputBusses) {
        List<ItemOutputBusBlockEntity> itemBusses = outputBusses.stream()
                .filter(bus -> bus.getBusType() == BusType.ITEM)
                .map(bus -> (ItemOutputBusBlockEntity) bus)
                .collect(Collectors.toList());

        ItemStack remaining = stack.copy();
        for (ItemOutputBusBlockEntity bus : itemBusses) {
            if (remaining.isEmpty()) break;
            ItemStackHandler handler = bus.getItemHandler();
            
            for (int i = 0; i < handler.getSlots(); i++) {
                if (remaining.isEmpty()) break;
                ItemStack existing = handler.getStackInSlot(i);
                
                if (existing.isEmpty()) {
                    int toInsert = Math.min(remaining.getCount(), Math.min(handler.getSlotLimit(i), remaining.getMaxStackSize()));
                    handler.setStackInSlot(i, remaining.copyWithCount(toInsert));
                    remaining.shrink(toInsert);
                } else if (ItemStack.isSameItemSameComponents(existing, remaining)) {
                    int maxStack = Math.min(handler.getSlotLimit(i), remaining.getMaxStackSize());
                    int canInsert = maxStack - existing.getCount();
                    if (canInsert > 0) {
                        int toInsert = Math.min(canInsert, remaining.getCount());
                        handler.setStackInSlot(i, existing.copyWithCount(existing.getCount() + toInsert));
                        remaining.shrink(toInsert);
                    }
                }
            }
        }
    }

    private void insertFluid(FluidStack stack, List<AbstractBusBlockEntity> outputBusses) {
        List<FluidOutputBusBlockEntity> fluidBusses = outputBusses.stream()
                .filter(bus -> bus.getBusType() == BusType.FLUID)
                .map(bus -> (FluidOutputBusBlockEntity) bus)
                .collect(Collectors.toList());

        int remaining = stack.getAmount();
        for (FluidOutputBusBlockEntity bus : fluidBusses) {
            if (remaining <= 0) break;
            FluidStack existing = bus.getFluidTank().getFluid();
            if (existing.isEmpty()) {
                bus.getFluidTank().fill(new FluidStack(stack.getFluid(), remaining), IFluidHandler.FluidAction.EXECUTE);
                remaining = 0;
            } else if (existing.isFluidEqual(stack)) {
                int canInsert = bus.getFluidTank().getCapacity() - existing.getAmount();
                int toInsert = Math.min(canInsert, remaining);
                bus.getFluidTank().fill(new FluidStack(stack.getFluid(), toInsert), IFluidHandler.FluidAction.EXECUTE);
                remaining -= toInsert;
            }
        }
    }

    public ReadOnlyEnergyStorage getEnergyStorageReadOnly(Direction direction) {
        if (this.getEnergyStorage() == null) return null;
        return new ReadOnlyEnergyStorage(this.getEnergyStorage());
    }

    public ReadOnlyItemHandler getItemHandlerReadOnly(Direction direction) {
        if (this.getItemHandler() == null) return null;
        return new ReadOnlyItemHandler(this.getItemHandler());
    }

    public ReadOnlyFluidHandler getFluidHandlerReadOnly(Direction direction) {
        if (this.getFluidHandler() == null) return null;
        return new ReadOnlyFluidHandler(this.getFluidHandler());
    }

    @Override
    public UpgradeItemHandler getUpgradeItemHandler() {
        return this.getHandler(UPGRADES_ID);
    }

    @Override
    public Set<ResourceKey<UpgradeType>> getSupportedUpgrades() {
        return SUPPORTED_UPGRADES;
    }

    @Override
    public boolean hasUpgrade(ResourceKey<UpgradeType> upgrade) {
        for (int i = 0; i < this.getUpgradeItemHandler().getSlots(); i++) {
            ItemStack stack = this.getUpgradeItemHandler().getStackInSlot(i);
            if (stack.getItem() instanceof UpgradeItem upgradeItem && upgradeItem.getUpgradeTypeKey().equals(upgrade)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getUpgradeAmount(ResourceKey<UpgradeType> upgrade) {
        int amount = 0;

        for (int i = 0; i < this.getUpgradeItemHandler().getSlots(); i++) {
            ItemStack stackInSlot = this.getUpgradeItemHandler().getStackInSlot(i);
            if (stackInSlot.getItem() instanceof UpgradeItem upgradeItem && upgradeItem.getUpgradeTypeKey().equals(upgrade)) {
                amount += stackInSlot.getCount();
            }
        }

        return amount;
    }

    @Override
    public void onUpgradeAdded(ResourceKey<UpgradeType> upgrade) {

    }

    @Override
    public void onUpgradeRemoved(ResourceKey<UpgradeType> upgrade) {

    }

    @Override
    public IItemHandler getItemHandlerOnSide(Direction direction) {
        return null;
    }

    @Override
    public MultiblockData getMultiblockData() {
        return this.multiblockData;
    }

    @Override
    public void setMultiblockData(MultiblockData multiblockData) {
        this.multiblockData = multiblockData;
        this.setChanged();
    }

    @Override
    protected void saveData(CompoundTag tag, HolderLookup.Provider registries) {
        if (this.getMultiblockData() != null) {
            tag.put("multiblock_data", this.saveMBData());
        }

        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        tag.putInt("energyPerTick", energyPerTick);
        tag.putBoolean("isProcessing", isProcessing);
        tag.putBoolean("isRegularRecipe", isRegularRecipe);
        
        if (isProcessing) {
            if (isRegularRecipe && currentRegularRecipeId != null) {
                tag.putString("currentRegularRecipeId", currentRegularRecipeId.toString());
            } else if (!isRegularRecipe && currentPowerRecipeId != null) {
                tag.putString("currentPowerRecipeId", currentPowerRecipeId.toString());
            }
        }
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        tag.putInt("energyPerTick", energyPerTick);
        tag.putBoolean("isProcessing", isProcessing);
        tag.putBoolean("isRegularRecipe", isRegularRecipe);
        
        tag.putInt("totalEnergy", getTotalInputEnergy(getInputBusses()));
        tag.putInt("maxEnergy", getTotalMaxInputEnergy());
        
        if (isProcessing && isRegularRecipe && currentRegularRecipe != null && this.level != null) {
            CompoundTag recipeTag = new CompoundTag();
            recipeTag.putString("recipeType", "regular");
            
            var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), this.level.registryAccess());
            PlanetSimulatorRecipe.Serializer.STREAM_CODEC.encode(buf, currentRegularRecipe);
            byte[] recipeData = new byte[buf.readableBytes()];
            buf.readBytes(recipeData);
            recipeTag.putByteArray("recipeData", recipeData);
            buf.release();
            
            tag.put("activeRecipe", recipeTag);
        }
        
        return tag;
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("multiblock_data")) {
            this.multiblockData = this.loadMBData(tag.getCompound("multiblock_data"));
        }

        progress = tag.getInt("progress");
        maxProgress = tag.getInt("maxProgress");
        energyPerTick = tag.getInt("energyPerTick");
        isProcessing = tag.getBoolean("isProcessing");
        isRegularRecipe = tag.getBoolean("isRegularRecipe");
        currentRegularRecipe = null;
        currentRegularRecipeId = null;
        currentPowerRecipe = null;
        currentPowerRecipeId = null;
        
        if (isProcessing) {
            if (tag.contains("currentRegularRecipeId", Tag.TAG_STRING)) {
                ResourceLocation id = ResourceLocation.tryParse(tag.getString("currentRegularRecipeId"));
                if (id == null) {
                    Spaceploitation.LOGGER.error("Invalid regular recipe id '{}' found while loading Planet Simulator at {}", tag.getString("currentRegularRecipeId"), this.worldPosition);
                    resetProgress();
                } else {
                    currentRegularRecipeId = id;
                    if (this.level != null && !this.level.isClientSide) {
                        if (!resolveRegularRecipe()) {
                            resetProgress();
                        }
                    }
                }
            } else if (tag.contains("currentPowerRecipeId", Tag.TAG_STRING)) {
                ResourceLocation id = ResourceLocation.tryParse(tag.getString("currentPowerRecipeId"));
                if (id == null) {
                    Spaceploitation.LOGGER.error("Invalid power recipe id '{}' found while loading Planet Simulator at {}", tag.getString("currentPowerRecipeId"), this.worldPosition);
                    resetProgress();
                } else {
                    currentPowerRecipeId = id;
                    if (this.level != null && !this.level.isClientSide) {
                        if (!resolvePowerRecipe()) {
                            resetProgress();
                        }
                    }
                }
            }
        }
        
        if (this.level != null && this.level.isClientSide) {
            clientTotalEnergy = tag.getInt("totalEnergy");
            clientMaxEnergy = tag.getInt("maxEnergy");
            
            if (tag.contains("activeRecipe")) {
                CompoundTag recipeTag = tag.getCompound("activeRecipe");
                String recipeType = recipeTag.getString("recipeType");
                
                if (recipeType.equals("regular")) {
                    byte[] recipeData = recipeTag.getByteArray("recipeData");
                    var buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(recipeData), this.level.registryAccess());
                    try {
                        PlanetSimulatorRecipe recipe = PlanetSimulatorRecipe.Serializer.STREAM_CODEC.decode(buf);
                        clientOutputs = recipe.outputs();
                    } catch (Exception e) {
                        Spaceploitation.LOGGER.error("Failed to decode recipe from update tag", e);
                    } finally {
                        buf.release();
                    }
                }
            } else {
                clientOutputs = List.of();
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Planet Simulator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new PlanetSimulatorMenu(i, inventory, this);
    }
}
