package gregtech.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IControllable;
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
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MetaTileEntityHPCA extends MultiblockWithDisplayBase implements IControllable {

    // Match Context Headers
    private static final String HPCA_COMPONENT_HEADER = "HPCAComponents";

    private final HPCAGridHandler hpcaHandler = new HPCAGridHandler();

    public MetaTileEntityHPCA(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityHPCA(metaTileEntityId);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        Object2ObjectMap<BlockPos, IHPCAComponent> components = context.get(HPCA_COMPONENT_HEADER);
        hpcaHandler.onStructureForm(components.values());
        System.out.println("Hints:");
        System.out.println(hpcaHandler.getPossibleHints());
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
                        abilities(MultiblockAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1)//.or(
                        //abilities(MultiblockAbility.COMPUTATION_DATA_TRANSMISSION).setExactLimit(1))
                        ))))
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
            Object didHaveOld = components.put(state.getPos(), component);
            if (didHaveOld != null) System.out.println("yeah, something old was there for some reason");
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

    // todo
    @Override
    public boolean isWorkingEnabled() {
        return false;
    }

    // todo
    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {

    }

    // Handles the logic of this structure's specific HPCA component grid
    private static class HPCAGridHandler {

        private List<IHPCAComponent> currentComponents; // todo test if Set will work here

        private int coolantPerTick = -1; // todo
        private int maximumCWUt = -1;
        private long passiveEUt = -1;
        private long maximumEUt = -1;
        private List<String> hints = null;

        private void onStructureForm(Collection<IHPCAComponent> components) {
            // reset state, lazily evaluated
            currentComponents = new ArrayList<>(components);
            coolantPerTick = -1;
            maximumCWUt = -1;
            passiveEUt = -1;
            maximumEUt = -1;
            hints = null;
        }

        /** The amount of coolant needed to run every tick */
        private int getCoolantPerTick() {
            if (coolantPerTick == -1) {
                // todo recalculate
            }
            return coolantPerTick;
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
        private long getPassiveEUt() {
            if (passiveEUt == -1) {
                passiveEUt = 0;
                for (var component : currentComponents) {
                    passiveEUt += component.getUpkeepEUt();
                }
            }
            return passiveEUt;
        }

        /** The maximum EU/t drain for this multi. EU/t will be proportional to the amount of computation used. */
        private long getMaxEUt() {
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

                // todo cooling warning
            }
            return hints;
        }
    }
}
