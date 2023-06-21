package gregtech.api.capability;

public interface IOpticalComputationHatch {

    /** If this hatch transmits or receives CWU/t. */
    boolean isTransmitter();

    /**
     * Request a specified amount of CWU/t (Compute Work Units per tick).
     * Transmitters will ping their attached Multiblock Controller ({@link IOpticalComputationProvider}).
     * Receivers will ping their attached Transmitter (an instanceof this interface).
     *
     * @param cwut How much CWU/t to try and get.
     * @return How much CWU/t is available.
     */
    int requestCWUt(int cwut, boolean simulate);
}
