package gregtech.common.metatileentities.multi.electric;

import gregtech.api.capability.IObjectHolder;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMaps;
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

import java.util.List;

public class MetaTileEntityResearchStation extends RecipeMapMultiblockController {

    public MetaTileEntityResearchStation(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.RESEARCH_STATION_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityResearchStation(metaTileEntityId);
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "VVV", "PPP", "PPP", "PPP", "VVV", "XXX")
                .aisle("XXX", "VAV", "AAA", "AAA", "AAA", "VAV", "XXX")
                .aisle("XXX", "VAV", "XAX", "XSX", "XAX", "VAV", "XXX")
                .aisle("XXX", "XAX", "---", "---", "---", "XAX", "XXX")
                .aisle(" X ", "XAX", "---", "---", "---", "XAX", " X ")
                .aisle(" X ", "XAX", "-A-", "-H-", "-A-", "XAX", " X ")
                .aisle("   ", "XXX", "---", "---", "---", "XXX", "   ")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()))
                .where(' ', any())
                .where('-', air())
                .where('V', states(getVentState()))
                .where('A', states(getAdvancedState()))
                .where('P', states(getCasingState()).or(
                        abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).or(
                        abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1).or(
                        abilities(MultiblockAbility.COMPUTATION_DATA_RECEPTION).setExactLimit(1)))))
                .where('H', abilities(MultiblockAbility.OBJECT_HOLDER)) // todo fix facing, should be forced to facing the controller
                .build();
    }

    @NotNull
    private static IBlockState getVentState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.COMPUTER_HEAT_VENT);
    }

    @NotNull
    private static IBlockState getAdvancedState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.ADVANCED_COMPUTER_CASING);
    }

    @NotNull
    private static IBlockState getCasingState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.COMPUTER_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (sourcePart == null || sourcePart instanceof IObjectHolder) {
            return Textures.ADVANCED_COMPUTER_CASING;
        }
        return Textures.COMPUTER_CASING;
    }

    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.RESEARCH_STATION_OVERLAY;
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.research_station.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.research_station.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.research_station.tooltip.3"));
    }
}
