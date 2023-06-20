package gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.IOpticalComputationHatch;
import gregtech.api.capability.IOpticalComputationProvider;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.util.GTLog;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class MetaTileEntityComputationHatch extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IOpticalComputationHatch>, IOpticalComputationHatch {

    private final boolean isTransmitter;

    public MetaTileEntityComputationHatch(ResourceLocation metaTileEntityId, boolean isTransmitter) {
        super(metaTileEntityId, GTValues.ZPM);
        this.isTransmitter = isTransmitter;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityComputationHatch(metaTileEntityId, isTransmitter);
    }

    @Override
    public boolean isTransmitter() {
        return this.isTransmitter;
    }

    @Override
    public int requestCWUt(int cwut) {
        if (isTransmitter()) {
            // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
            if (getController() instanceof IOpticalComputationProvider provider) {
                return provider.requestCWUt(cwut);
            } else {
                GTLog.logger.error("Computation Transmission Hatch could not get CWU/t from its controller!");
                return 0;
            }
        } else {
            // Ask the attached Transmitter hatch, if it exists
            // todo
            return 0;
        }
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            // todo make its own texture
            Textures.OPTICAL_DATA_ACCESS_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public MultiblockAbility<IOpticalComputationHatch> getAbility() {
        return isTransmitter()
                ? MultiblockAbility.COMPUTATION_DATA_TRANSMISSION
                : MultiblockAbility.COMPUTATION_DATA_RECEPTION;
    }

    @Override
    public void registerAbilities(List<IOpticalComputationHatch> abilityList) {
        abilityList.add(this);
    }
}
