package gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.IObjectHolder;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class MetaTileEntityObjectHolder extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IObjectHolder>, IObjectHolder {

    public MetaTileEntityObjectHolder(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.ZPM);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityObjectHolder(metaTileEntityId);
    }

    // todo
    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public MultiblockAbility<IObjectHolder> getAbility() {
        return MultiblockAbility.OBJECT_HOLDER;
    }

    @Override
    public void registerAbilities(List<IObjectHolder> abilityList) {
        abilityList.add(this);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        SimpleOverlayRenderer renderer = Textures.OBJECT_HOLDER_OVERLAY;
        var controller = getController();
        if (controller != null && controller.isActive()) {
            renderer = Textures.OBJECT_HOLDER_ACTIVE_OVERLAY;
        }
        renderer.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }
}
