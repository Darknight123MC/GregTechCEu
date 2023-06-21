package gregtech.api.capability;

/**
 * MUST be implemented on any multiblock which uses
 * Transmitter Computation Hatches in its structure.
 */
public interface IOpticalComputationProvider {

    /**
     * Request some amount of CWU/t (Compute Work Units per tick) from this Machine.
     * Implementors should expect these requests to occur each tick that computation is required.
     *
     * @param cwut Maximum amount of CWU/t requested.
     * @return The amount of CWU/t that could be supplied.
     */
    int requestCWUt(int cwut, boolean simulate);

    /**
     * Whether this Computation Provider can "Bridge" with other Computation Providers.
     * Checked by machines like the Network Switch.
     */
    boolean canBridge();
}
