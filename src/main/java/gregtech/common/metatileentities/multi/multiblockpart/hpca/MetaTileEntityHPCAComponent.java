package gregtech.common.metatileentities.multi.multiblockpart.hpca;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.IHPCAComponentHatch;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public abstract class MetaTileEntityHPCAComponent extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IHPCAComponentHatch>, IHPCAComponentHatch {

    public MetaTileEntityHPCAComponent(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.ZPM);
    }

    public abstract boolean isAdvanced();

    public abstract SimpleOverlayRenderer getFrontOverlay();

    public SimpleOverlayRenderer getFrontActiveOverlay() {
        return getFrontOverlay();
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
    public MultiblockAbility<IHPCAComponentHatch> getAbility() {
        return MultiblockAbility.HPCA_COMPONENT;
    }

    @Override
    public void registerAbilities(List<IHPCAComponentHatch> abilityList) {
        abilityList.add(this);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer;
            var controller = getController();
            if (controller != null && controller.isActive()) {
                renderer = getFrontActiveOverlay();
            } else {
                renderer = getFrontOverlay();
            }
            if (renderer != null) {
                EnumFacing facing = getFrontFacing();
                // always render this outwards, in case it is not placed outwards in structure
                if (controller != null) {
                    facing = controller.getFrontFacing().rotateY();
                }
                renderer.renderSided(facing, renderState, translation, pipeline);
            }
        }
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        return isAdvanced() ? Textures.ADVANCED_COMPUTER_CASING : Textures.COMPUTER_CASING;
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    public boolean canPartShare() {
        return false;
    }
}
