package gregtech.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockComputerCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.multi.multiblockpart.hpca.MetaTileEntityHPCABridge;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MetaTileEntityHPCA extends MultiblockWithDisplayBase implements IOpticalComputationProvider, IControllable {

    private static final int IDLE_TEMPERATURE = 0;
    private static final int DAMAGE_TEMPERATURE = 1000;

    private IEnergyContainer energyContainer;
    private final HPCAGridHandler hpcaHandler = new HPCAGridHandler();

    private boolean isActive;
    private boolean isWorkingEnabled = true;
    private boolean hasNotEnoughEnergy;

    private int outputComputation;
    private int requestTimer;
    private int temperature;

    public MetaTileEntityHPCA(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityHPCA(metaTileEntityId);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.hpcaHandler.onStructureForm(getAbilities(MultiblockAbility.HPCA_COMPONENT));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
        this.hpcaHandler.onStructureInvalidate();
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate) {
        // todo
        return 0;
        //if (hasNotEnoughEnergy) {
        //    outputComputation = 0;
        //    requestTimer = 0;
        //    return 0;
        //}
        //int cwutToSend = Math.min(cwut, hpcaHandler.getMaxCWUt());
        //outputComputation = cwutToSend;
        //requestTimer = 2;
        //return cwutToSend;
    }

    @Override
    protected void updateFormedValid() {
        // energy
        boolean consumedEnough = consumeEnergy();
        if (!isActive()) {
            setActive(true);
        }

        // todo
        // temperature and coolant
        //calculateTemperature();
        //if (temperature >= DAMAGE_TEMPERATURE) {
            //boolean causedDamage = rollDamageComponent();
            //if (causedDamage) {
            //    invalidateStructure();
            //    return;
            //}
        //}

        // computation
        //if (requestTimer > 0) {
        //    if (consumedEnough) {
        //        setActive(true);
        //        requestTimer--;
        //        if (requestTimer == 0) {
        //            setActive(false);
        //            requestTimer = 0;
        //            outputComputation = 0;
        //        }
        //    } else {
        //        setActive(false);
        //        requestTimer = 0;
        //        outputComputation = 0;
        //    }
        //}
    }

    /** @return if successful in drawing energy */
    private boolean consumeEnergy() {
        int energyToConsume = getCurrentEUt();
        boolean hasMaintenance = ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics();
        if (hasMaintenance) {
            // 10% more energy per maintenance problem
            energyToConsume += getNumMaintenanceProblems() * energyToConsume / 10;
        }

        if (this.hasNotEnoughEnergy && energyContainer.getInputPerSec() > 19L * energyToConsume) {
            this.hasNotEnoughEnergy = false;
        }

        if (this.energyContainer.getEnergyStored() >= energyToConsume) {
            if (!hasNotEnoughEnergy) {
                long consumed = this.energyContainer.removeEnergy(energyToConsume);
                if (consumed == -energyToConsume) {
                    if (hasMaintenance) {
                        calculateMaintenance(1);
                    }
                    return true;
                } else {
                    this.hasNotEnoughEnergy = true;
                }
            }
        } else {
            this.hasNotEnoughEnergy = true;
        }
        return false;
    }

    /*
    private void calculateTemperature() {
        if (isActive()) {
            temperature += hpcaHandler.getMaxCoolantDemand() * (1.0 * outputComputation / hpcaHandler.getMaxCoolantDemand());
        }
        int temperatureToLower = Math.min(temperature, hpcaHandler.getMaxCoolantProduction()); // do not go below 0


        int coolantToUse = hpcaHandler.getMaximumActiveCooling();
        temperature -= temperatureToLower;
    }*/

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("AA", "CC", "CC", "CC", "AA")
                .aisle("VA", "XV", "XV", "XV", "VA")
                .aisle("VA", "XV", "XV", "XV", "VA")
                .aisle("VA", "XV", "XV", "XV", "VA")
                .aisle("SA", "CC", "CC", "CC", "AA")
                .where('S', selfPredicate())
                .where('A', states(getAdvancedState()))
                .where('V', states(getVentState()))
                .where('X', abilities(MultiblockAbility.HPCA_COMPONENT))
                .where('C', states(getCasingState()).or(
                        abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1).or(
                        abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).or(
                        abilities(MultiblockAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1).or(
                        abilities(MultiblockAbility.COMPUTATION_DATA_TRANSMISSION).setExactLimit(1))))))
                .build();
    }

    @NotNull
    private static IBlockState getCasingState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.COMPUTER_CASING);
    }

    @NotNull
    private static IBlockState getAdvancedState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.ADVANCED_COMPUTER_CASING);
    }

    @NotNull
    private static IBlockState getVentState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.COMPUTER_HEAT_VENT);
    }

    // todo example JEI structures

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (sourcePart == null) {
            return Textures.ADVANCED_COMPUTER_CASING; // controller
        }
        return Textures.COMPUTER_CASING; // multiblock parts
    }

    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.HPCA_OVERLAY;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), this.isActive(), this.isWorkingEnabled());
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.isActive;
    }

    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            markDirty();
            if (getWorld() != null && !getWorld().isRemote) {
                writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (this.isWorkingEnabled != isWorkingAllowed) {
            this.isWorkingEnabled = isWorkingAllowed;
            markDirty();
            if (getWorld() != null && !getWorld().isRemote) {
                writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
            }
        }
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed()) {
            //textList.add(new TextComponentString(String.format("Maximum Computation: %d CWU/t", hpcaHandler.getMaxCWUt())));
            //textList.add(new TextComponentString(String.format("Maximum Power: %d EU/t", hpcaHandler.getMaxEUt())));
            //textList.add(new TextComponentString(String.format("Current Power Usage: %d EU/t", getCurrentEUt())));
            //textList.add(new TextComponentString(String.format("Maximum Coolant Demand: %d CU/t", hpcaHandler.getMaxCoolantDemand())));
            //textList.add(new TextComponentString(String.format("Maximum Coolant Supply: %d CU/t", hpcaHandler.getMaxCoolantProduction())));

            List<String> hints = hpcaHandler.getPossibleHints();
            if (!hints.isEmpty()) {
                ITextComponent hint0 = new TextComponentString(hints.get(0));
                if (hints.size() > 1) {
                    for (int i = 1; i < hints.size(); i++) {
                        hint0.appendSibling(new TextComponentString(hints.get(i)));
                    }
                }

                ITextComponent hintComponent = new TextComponentString("Potential structure improvements: (hover)").setStyle(new Style().setColor(TextFormatting.RED)
                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hint0)));
                textList.add(hintComponent);
            }

            //textList.add(new TextComponentString(String.format("Maximum Coolant: %s L/t", hpcaHandler.getMaximumActiveCooling())));
            //textList.add(new TextComponentTranslation("gregtech.multiblock.energy_consumption", this.hpcaHandler.));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.high_performance_computing_array.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.high_performance_computing_array.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.high_performance_computing_array.tooltip.3"));
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }

    // todo
    private int getCurrentEUt() {
        return isStructureFormed() ? hpcaHandler.getUpkeepEUt() : 0;
        //if (isStructureFormed()) {
        //    if (isActive()) {
        //        // energy draw is proportional to the amount of actively used computation
        //        // (b - a) * (c / d) + a
        //        return (int) ((hpcaHandler.getMaxEUt() - hpcaHandler.getPassiveEUt()) * (1.0 * outputComputation / hpcaHandler.getMaxCWUt()) + hpcaHandler.getPassiveEUt());
        //    } else {
        //        return hpcaHandler.getPassiveEUt();
        //    }
        //}
        //return 0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public SoundEvent getSound() {
        return GTSoundEvents.COMPUTATION;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isActive", this.isActive);
        data.setBoolean("isWorkingEnabled", this.isWorkingEnabled);
        data.setInteger("temperature", this.temperature);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isActive = data.getBoolean("isActive");
        this.isWorkingEnabled = data.getBoolean("isWorkingEnabled");
        this.temperature = data.getInteger("temperature");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isActive);
        buf.writeBoolean(this.isWorkingEnabled);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
        this.isWorkingEnabled = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            this.isActive = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.isWorkingEnabled = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    // Handles the logic of this structure's specific HPCA component grid
    private static class HPCAGridHandler {

        // structure info
        private final Set<IHPCAComponentHatch> components = new HashSet<>();
        private final Set<IHPCACoolantProvider> coolantProviders = new HashSet<>();
        private final Set<IHPCAComputationProvider> computationProviders = new HashSet<>();
        private int numBridges;

        private int maximumCoolantPerTick = -1;
        private int totalMaximumCooling = -1;
        private int maximumActiveCooling = -1;
        private int maximumCWUt = -1;
        private int maximumEUt = -1;

        private void onStructureForm(Collection<IHPCAComponentHatch> components) {
            reset();
            for (var component : components) {
                this.components.add(component);
                if (component instanceof IHPCACoolantProvider coolantProvider) {
                    this.coolantProviders.add(coolantProvider);
                }
                if (component instanceof IHPCAComputationProvider computationProvider) {
                    this.computationProviders.add(computationProvider);
                }
                if (component instanceof MetaTileEntityHPCABridge) {
                    this.numBridges++;
                }
            }
        }

        private void onStructureInvalidate() {
            reset();
        }

        private void reset() {
            // reset state, lazily evaluated
            components.clear();
            coolantProviders.clear();
            computationProviders.clear();
            numBridges = 0;

            // todo ???
            maximumCoolantPerTick = -1;
            totalMaximumCooling = -1;
            maximumActiveCooling = -1;
            maximumCWUt = -1;
            maximumEUt = -1;
        }

        // todo is this how this should be done? idk
        /** The passive EU/t drain for this multi. EU/t will never be lower than this. */
        private int getUpkeepEUt() {
            int upkeepEUt = 0;
            for (var component : components) {
                upkeepEUt += component.getUpkeepEUt();
            }
            return upkeepEUt;
        }


        /** The maximum amount of "coolant" this could need if running at 100% *//*
        private int getMaxCoolantDemand() {
            if (maximumCoolantPerTick == -1) {
                maximumCoolantPerTick = components.stream()
                        .mapToInt(IHPCAComponent::getMaxCoolantDemandPerTick)
                        .sum();
            }
            return maximumCoolantPerTick;
        }*/

        /** How much "coolant" this can currently make. *//*
        private int getMaxCoolantProduction() {
            if (totalMaximumCooling == -1) {
                totalMaximumCooling = components.stream()
                        .filter(IHPCAComponent::isCoolantProvider)
                        .mapToInt(IHPCAComponent::getMaxCoolantPerTick)
                        .sum();
            }
            return totalMaximumCooling;
        }*/

        /** Maximum amount of coolant in L/t to consume if running at 100% computation. *//*
        private int getMaximumActiveCooling() {
            if (maximumActiveCooling == -1) {
                maximumActiveCooling = components.stream()
                        .filter(IHPCAComponent::isCoolantProvider)
                        .mapToInt(IHPCAComponent::getMaxActiveCoolantPerTick)
                        .sum();
            }
            return maximumActiveCooling;
        }*/

        /** The maximum amount of CWUs (Compute Work Units) created per tick. *//*
        private int getMaxCWUt() {
            if (maximumCWUt == -1) {
                maximumCWUt = components.stream()
                        .filter(IHPCAComponent::isCWUProvider)
                        .mapToInt(IHPCAComponent::getMaxCWUPerTick)
                        .sum();
            }
            return maximumCWUt;
        }*/

        /** The maximum EU/t drain for this multi. EU/t will be proportional to the amount of computation used. *//*
        private int getMaxEUt() {
            if (maximumEUt == -1) {
                maximumEUt = 0;
                for (var component : currentComponents) {
                    maximumEUt += component.getMaxEUt();
                }
            }
            return maximumEUt;
        }*/

        /**
         * A list of hints for the controller to display.
         * Used to recommend potential better solutions when simple mistakes are made
         * (such as multiple HPCA Bridge blocks being present, which offers no additional benefit).
         *
         * @return empty list if no hints.
         */
        @NotNull
        private List<String> getPossibleHints() {
            List<String> hints = new ArrayList<>();

            // Damaged component present
            if (components.stream().anyMatch(IHPCAComponentHatch::isDamaged)) {
                hints.add("Damaged HPCA Component found in structure!");
            }

            // More than 1 bridge present
            if (numBridges > 1) {
                hints.add("More HPCA Bridges than necessary, more than one provides no additional benefit!");
            }

            // No computation units present
            if (computationProviders.isEmpty()) {
                hints.add("No HPCA Computation providers found! No computation can be done");
            }

            // todo
            //if (getMaxCoolantDemand() > getMaxCoolantProduction()) {
            //    hints.add("HPCA will overheat if run at maximum computation, not enough coolant potential!");
            //}
            return hints;
        }
    }
}
