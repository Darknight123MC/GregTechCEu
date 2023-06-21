package gregtech.common.metatileentities.multi.multiblockpart.hpca;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IHPCAComputationProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.blocks.BlockComputerCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
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
            markDirty();
            if (getWorld() != null && !getWorld().isRemote) {
                writeCustomData(GregtechDataCodes.DAMAGE_STATE, buf -> buf.writeBoolean(damaged));
            }
        }
    }

    @Override
    public int getCWUPerTick() {
        if (isDamaged()) return 0;
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

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(damaged);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.damaged = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.DAMAGE_STATE) {
            this.damaged = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public ItemStack getStackForm(int amount) {
        if (isDamaged()) {
            if (isAdvanced()) {
                return MetaBlocks.COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.DAMAGED_ADVANCED_HPCA_COMPONENT, amount);
            } else {
                return MetaBlocks.COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.DAMAGED_HPCA_COMPONENT, amount);
            }
        }
        return super.getStackForm(amount);
    }
}
