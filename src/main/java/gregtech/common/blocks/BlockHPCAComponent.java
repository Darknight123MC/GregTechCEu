package gregtech.common.blocks;

import gregtech.api.GTValues;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.metatileentity.multiblock.IHPCAComponent;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

public class BlockHPCAComponent extends VariantActiveBlock<BlockHPCAComponent.ComponentType> {

    public BlockHPCAComponent() {
        super(Material.IRON);
        setTranslationKey("hpca_component");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(ComponentType.HPCA_EMPTY));
    }

    public enum ComponentType implements IStringSerializable, IHPCAComponent {

        HPCA_EMPTY("hpca_empty"), // Does nothing; just fills a block space if you don't want/need anything else
        HPCA_HEAT_SINK("hpca_heat_sink"), // Passive cooling, does not require coolant/t
        HPCA_ACTIVE_COOLER("hpca_active_cooler"), // More effective cooling, but requires a coolant/t
        HPCA_COMPUTATION("hpca_computation"), // Creates an amount of CWU/t (and requires some cooling)
        HPCA_COMPUTATION_HIGH("hpca_computation_high"), // higher-tier computation cost, but needs much more cooling
        HPCA_BRIDGE("hpca_bridge"), // Allows connection to other HPCAs
        HPCA_DAMAGED("hpca_damaged"), // Damaged parts, the result of running too hot for too long
        HPCA_DAMAGED_ADVANCED("hpca_damaged_advanced"),
        ;

        public static final ComponentType[] VALUES = values();

        private final String name;

        ComponentType(String name) {
            this.name = name;
        }

        @NotNull
        @Override
        public String getName() {
            return name;
        }

        @NotNull
        @Override
        public String toString() {
            return getName();
        }

        // Computation has low upkeep, but much higher max.
        // Other parts have no increase in EU/t when active, so their
        // upkeep cost is higher.
        @Override
        public int getUpkeepEUt() {
            return switch (this) {
                case HPCA_ACTIVE_COOLER,
                        HPCA_BRIDGE,
                        HPCA_COMPUTATION_HIGH -> GTValues.VA[GTValues.IV];
                case HPCA_COMPUTATION         -> GTValues.VA[GTValues.EV];

                default -> 0;
            };
        }

        @Override
        public int getMaxEUt() {
            return switch (this) {
                // same as upkeep cost, this amount doesn't change
                case HPCA_ACTIVE_COOLER, HPCA_BRIDGE -> GTValues.VA[GTValues.IV];

                // maximum possible EU/t if full computation is being used
                case HPCA_COMPUTATION      -> GTValues.VA[GTValues.LuV];
                case HPCA_COMPUTATION_HIGH -> GTValues.VA[GTValues.ZPM];

                default -> 0;
            };
        }


        //// Coolant Providers

        // todo make sure these numbers are ok
        @Override
        public int getMaxCoolantPerTick() {
            return switch (this) {
                case HPCA_HEAT_SINK     -> 32;

                // Active cooler provides 2x the cooling amount
                case HPCA_ACTIVE_COOLER -> 64;

                // -1 for everything else, to mark as "not a coolant provider"
                default -> -1;
            };
        }

        // todo make sure active cooler number is ok (is the L/t drawn)
        @Override
        public int getMaxActiveCoolantPerTick() {
            return this == HPCA_ACTIVE_COOLER ? 64 : 0;
        }


        //// CWU/t Providers

        @Override
        public int getMaxCoolantDemandPerTick() {
            return switch (this) {
                case HPCA_COMPUTATION      -> 64;
                case HPCA_COMPUTATION_HIGH -> 128;

                default -> 0;
            };
        }

        @Override
        public int getMaxCWUPerTick() {
            return switch (this) {
                case HPCA_COMPUTATION -> 4;
                case HPCA_COMPUTATION_HIGH -> 16; // todo make sure these are ok

                // -1 for everything else, to mark as "not a CWU/t provider"
                default -> -1;
            };
        }


        // Special Functionality

        @Override
        public boolean isHCPABridge() {
            return this == HPCA_BRIDGE;
        }

        @Override
        public boolean isDamaged() {
            return this == HPCA_DAMAGED || this == HPCA_DAMAGED_ADVANCED;
        }
    }
}
