package gregtech.api.metatileentity.multiblock;

public interface IHPCAComponent {

    /**
     * How much EU/t this component needs for the multi to just be idle.
     * Used in 2 ways:
     * - "Non-computational" units like HPCA Bridge, Active Cooler
     * - "Computational base cost" for units like HPCA Computation, High Computation
     */
    int getUpkeepEUt();

    /**
     * How much EU/t this component can use, if it is being utilized fully.
     * Used to scale cost for "computational" units. Power draw is a range
     * created by actual computation used vs maximum potential computation.
     */
    int getMaxEUt();


    //// Coolant Providers

    /** How much active OR passive cooling, in units/t, this component can provide to the HPCA. */
    int getMaxCoolantPerTick();

    /** How much active cooling, in L/t, this component can draw. */
    int getMaxActiveCoolantPerTick();

    /** Can this component supply coolant at all (active or passive) */
    default boolean isCoolantProvider() {
        return getMaxCoolantPerTick() > -1;
    }


    //// CWU/t Providers

    /** How much coolant/t this component needs when running at max CWU/t. */
    int getMaxCoolantDemandPerTick();

    /** How much CWU/t this component can make, if it is being utilized fully. */
    int getMaxCWUPerTick();

    /** Can this component supply CWU/t at all. */
    default boolean isCWUProvider() {
        return getMaxCWUPerTick() > -1;
    }


    //// Special functionality

    boolean isHCPABridge();

    boolean isDamaged();
}
