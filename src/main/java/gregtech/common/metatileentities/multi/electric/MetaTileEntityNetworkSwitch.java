package gregtech.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.IOpticalComputationHatch;
import gregtech.api.capability.IOpticalComputationProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockComputerCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MetaTileEntityNetworkSwitch extends MetaTileEntityDataBank implements IOpticalComputationProvider {

    private final MultipleComputationHandler computationHandler = new MultipleComputationHandler();

    public MetaTileEntityNetworkSwitch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityNetworkSwitch(metaTileEntityId);
    }

    @Override
    protected int calculateEnergyUsage() {
        int receivers = getAbilities(MultiblockAbility.COMPUTATION_DATA_RECEPTION).size();
        int transmitters = getAbilities(MultiblockAbility.COMPUTATION_DATA_TRANSMISSION).size();
        return GTValues.VA[GTValues.IV] * (receivers + transmitters);
    }

    @Override
    protected void updateFormedValid() {
        super.updateFormedValid();
        if (isActive() && !hasNotEnoughEnergy) {
            computationHandler.tick();
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        computationHandler.onStructureForm(
                getAbilities(MultiblockAbility.COMPUTATION_DATA_RECEPTION),
                getAbilities(MultiblockAbility.COMPUTATION_DATA_TRANSMISSION));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        computationHandler.reset();
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        if (!active) {
            computationHandler.clearRequests();
        }
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        super.setWorkingEnabled(isWorkingAllowed);
        if (!isWorkingAllowed) {
            computationHandler.clearRequests();
        }
    }

    // todo
    @Override
    public int requestCWUt(int cwut) {
        return 0;
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "XAX", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('S', selfPredicate())
                .where('A', states(getAdvancedState()))
                .where('X', states(getCasingState()).setMinGlobalLimited(7).or(
                        abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).or(
                        abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1).or(
                        abilities(MultiblockAbility.COMPUTATION_DATA_RECEPTION).setMinGlobalLimited(1).or(
                        abilities(MultiblockAbility.COMPUTATION_DATA_TRANSMISSION).setMinGlobalLimited(1))))))
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

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.COMPUTER_CASING;
    }

    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.NETWORK_SWITCH_OVERLAY;
    }

    @Override
    protected void renderTextures(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), this.isActive(), this.isWorkingEnabled());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.network_switch.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.network_switch.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.network_switch.tooltip.3"));
    }

    /** Handles computation load across multiple receivers and to multiple transmitters. */
    private static class MultipleComputationHandler {

        private List<IOpticalComputationHatch> providers; // todo can this be a set?
        private List<IOpticalComputationHatch> transmitters; // todo can this be a set?
        private final Set<ComputationRequest> activeRequests = new HashSet<>();

        private void tick() {
            if (activeRequests.size() > 0) {
                activeRequests.removeIf(ComputationRequest::tick);
            }
        }

        private void onStructureForm(Collection<IOpticalComputationHatch> providers, Collection<IOpticalComputationHatch> transmitters) {
            reset();
            this.providers = new ArrayList<>(providers);
            this.transmitters = new ArrayList<>(transmitters);
        }

        private void reset() {
            clearRequests();
            providers = null;
            transmitters = null;
        }

        /** Clear all current computation requests. For example: when machine runs out of energy or is disabled. */
        private void clearRequests() {
            activeRequests.clear();
        }
    }

    private static class ComputationRequest {

        private int remainingTime;

        private ComputationRequest() {
            remainingTime = 20;
        }

        /** @return If this request is expired. */
        private boolean tick() {
            remainingTime--;
            return remainingTime == 0;
        }
    }
}
