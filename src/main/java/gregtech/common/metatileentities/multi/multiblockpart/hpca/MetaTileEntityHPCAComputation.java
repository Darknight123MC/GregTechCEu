package gregtech.common.metatileentities.multi.multiblockpart.hpca;

import gregtech.api.GTValues;
import gregtech.api.capability.IHPCAComputationProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class MetaTileEntityHPCAComputation extends MetaTileEntityHPCAComponent implements IHPCAComputationProvider {

    private final boolean advanced;
    private boolean damaged;

    public MetaTileEntityHPCAComputation(ResourceLocation metaTileEntityId, boolean advanced) {
        super(metaTileEntityId);
        this.advanced = advanced;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityHPCAComputation(metaTileEntityId, advanced);
    }

    @Override
    public boolean isAdvanced() {
        return advanced;
    }

    @Override
    public SimpleOverlayRenderer getFrontOverlay() {
        if (isDamaged()) return advanced ? Textures.HPCA_ADVANCED_DAMAGED_OVERLAY : Textures.HPCA_DAMAGED_OVERLAY;
        return advanced ? Textures.HPCA_ADVANCED_COMPUTATION_OVERLAY : Textures.HPCA_COMPUTATION_OVERLAY;
    }

    @Override
    public SimpleOverlayRenderer getFrontActiveOverlay() {
        if (isDamaged()) return advanced ? Textures.HPCA_ADVANCED_DAMAGED_ACTIVE_OVERLAY : Textures.HPCA_DAMAGED_ACTIVE_OVERLAY;
        return advanced ? Textures.HPCA_ADVANCED_COMPUTATION_ACTIVE_OVERLAY : Textures.HPCA_COMPUTATION_ACTIVE_OVERLAY;
    }

    @Override
    public int getUpkeepEUt() {
        return GTValues.VA[advanced ? GTValues.IV : GTValues.EV];
    }

    @Override
    public int getMaxEUt() {
        return GTValues.VA[advanced ? GTValues.ZPM : GTValues.LuV];
    }

    @Override
    public boolean canBeDamaged() {
        return true;
    }

    @Override
    public boolean isDamaged() {
        return damaged;
    }

    @Override
    public void setDamaged(boolean damaged) {
        if (this.damaged != damaged) {
            this.damaged = damaged;
            // todo sync, client update etc
        }
    }

    @Override
    public int getCWUPerTick() {
        return advanced ? 16 : 4;
    }

    @Override
    public int getCoolantPerTick() {
        return advanced ? 4 : 2;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("damaged", damaged);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.damaged = data.getBoolean("damaged");
    }
}
