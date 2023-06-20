package gregtech.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IHPCAComponent;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockComputerCasing;
import gregtech.common.blocks.BlockHPCAComponent;
import gregtech.common.blocks.MetaBlocks;
import gregtech.core.sound.GTSoundEvents;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MetaTileEntityHPCA extends MultiblockWithDisplayBase implements IControllable {

    // Match Context Headers
    private static final String HPCA_COMPONENT_HEADER = "HPCAComponents";

    private IEnergyContainer energyContainer;
    private final HPCAGridHandler hpcaHandler = new HPCAGridHandler();

    private boolean isActive;
    private boolean isWorkingEnabled = true;

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
        Object2ObjectMap<BlockPos, IHPCAComponent> components = context.get(HPCA_COMPONENT_HEADER);
        this.hpcaHandler.onStructureForm(components.values());
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
        this.hpcaHandler.onStructureInvalidate();
    }

    @Override
    protected void updateFormedValid() {
        // todo :p
    }

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
                .where('X', hpcaComponentPredicate())
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

    @NotNull
    private static TraceabilityPredicate hpcaComponentPredicate() {
        return new TraceabilityPredicate(state -> {
            Block block = state.getBlockState().getBlock();
            if (!(block instanceof BlockHPCAComponent componentBlock)) {
                return false;
            }
            IHPCAComponent component = componentBlock.getState(state.getBlockState());
            Object2ObjectMap<BlockPos, IHPCAComponent> components = state.getMatchContext().get(HPCA_COMPONENT_HEADER);
            if (components == null) {
                components = new Object2ObjectOpenHashMap<>();
                state.getMatchContext().set(HPCA_COMPONENT_HEADER, components);
            }
            components.put(state.getPos(), component);
            return true;
        });
    }

    // todo example JEI structure

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
            textList.add(new TextComponentString(String.format("Maximum Computation: %d CWU/t", hpcaHandler.getMaxCWUt())));
            textList.add(new TextComponentString(String.format("Maximum Power: %d EU/t", hpcaHandler.getMaxEUt())));
            textList.add(new TextComponentString(String.format("Current Power Usage: %d EU/t", getCurrentEUt())));
            textList.add(new TextComponentString(String.format("Maximum Coolant Demand: %d CU/t", hpcaHandler.getMaxCoolantDemand())));
            textList.add(new TextComponentString(String.format("Maximum Coolant Supply: %d CU/t", hpcaHandler.getMaxCoolantProduction())));

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

    private int getCurrentEUt() {
        if (isStructureFormed()) {
            if (isActive()) {
                // todo
                return 0;
            } else {
                return hpcaHandler.getPassiveEUt();
            }
        }
        return 0;
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
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isActive = data.getBoolean("isActive");
        this.isWorkingEnabled = data.getBoolean("isWorkingEnabled");
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

        private List<IHPCAComponent> currentComponents; // todo test if Set will work here

        private int maximumCoolantPerTick = -1;
        private int totalMaximumCooling = -1;
        private int maximumActiveCooling = -1;
        private int maximumCWUt = -1;
        private int passiveEUt = -1;
        private int maximumEUt = -1;
        private List<String> hints = null;

        private void onStructureForm(Collection<IHPCAComponent> components) {
            reset();
            currentComponents = new ArrayList<>(components);
        }

        private void onStructureInvalidate() {
            reset();
        }

        private void reset() {
            // reset state, lazily evaluated
            currentComponents = null;
            maximumCoolantPerTick = -1;
            totalMaximumCooling = -1;
            maximumActiveCooling = -1;
            maximumCWUt = -1;
            passiveEUt = -1;
            maximumEUt = -1;
            hints = null;
        }

        /** The maximum amount of "coolant" this could need if running at 100% */
        private int getMaxCoolantDemand() {
            if (maximumCoolantPerTick == -1) {
                maximumCoolantPerTick = currentComponents.stream()
                        .mapToInt(IHPCAComponent::getMaxCoolantDemandPerTick)
                        .sum();
            }
            return maximumCoolantPerTick;
        }

        /** How much "coolant" this can currently make. */
        private int getMaxCoolantProduction() {
            if (totalMaximumCooling == -1) {
                totalMaximumCooling = currentComponents.stream()
                        .filter(IHPCAComponent::isCoolantProvider)
                        .mapToInt(IHPCAComponent::getMaxCoolantPerTick)
                        .sum();
            }
            return totalMaximumCooling;
        }

        /** Maximum amount of coolant to consume if running at 100% computation. */
        private int getMaximumActiveCooling() {
            if (maximumActiveCooling == -1) {
                maximumActiveCooling = currentComponents.stream()
                        .filter(IHPCAComponent::isCoolantProvider)
                        .mapToInt(IHPCAComponent::getMaxActiveCoolantPerTick)
                        .sum();
            }
            return maximumActiveCooling;
        }

        /** The maximum amount of CWUs (Compute Work Units) created per tick. */
        private int getMaxCWUt() {
            if (maximumCWUt == -1) {
                maximumCWUt = currentComponents.stream()
                        .filter(IHPCAComponent::isCWUProvider)
                        .mapToInt(IHPCAComponent::getMaxCWUPerTick)
                        .sum();
            }
            return maximumCWUt;
        }

        /** The passive EU/t drain for this multi. EU/t will never be lower than this. */
        private int getPassiveEUt() {
            if (passiveEUt == -1) {
                passiveEUt = 0;
                for (var component : currentComponents) {
                    passiveEUt += component.getUpkeepEUt();
                }
            }
            return passiveEUt;
        }

        /** The maximum EU/t drain for this multi. EU/t will be proportional to the amount of computation used. */
        private int getMaxEUt() {
            if (maximumEUt == -1) {
                maximumEUt = 0;
                for (var component : currentComponents) {
                    maximumEUt += component.getMaxEUt();
                }
            }
            return maximumEUt;
        }

        /**
         * A list of hints for the controller to display.
         * Used to recommend potential better solutions when simple mistakes are made
         * (such as multiple HPCA Bridge blocks being present, which offers no additional benefit).
         *
         * @return empty list if no hints.
         */
        @NotNull
        private List<String> getPossibleHints() {
            if (hints == null) {
                hints = new ArrayList<>();

                // Damaged component present
                if (currentComponents.stream().anyMatch(IHPCAComponent::isDamaged)) {
                    hints.add("Damaged HPCA Component found in structure!");
                }

                // More than 1 bridge present
                if (currentComponents.stream().filter(IHPCAComponent::isHCPABridge).count() > 1) {
                    hints.add("More HPCA Bridges than necessary, more than one provides no additional benefit!");
                }

                // No computation units present
                if (currentComponents.stream().noneMatch(IHPCAComponent::isCWUProvider)) {
                    hints.add("No HPCA Computation providers found! No computation can be done");
                }

                if (getMaxCoolantDemand() > getMaxCoolantProduction()) {
                    hints.add("HPCA will overheat if run at maximum computation, not enough coolant potential!");
                }
            }
            return hints;
        }
    }
}
