package com.hydroflow.exceptions;

/**
 * Thrown when distribution demand depletes reservoir storage below
 * the mandatory 15% emergency reserve volume.
 */
public class ReservoirDepletionException extends HydroFlowException {
    private final double currentVolumeM3;
    private final double reserveThresholdM3;

    public ReservoirDepletionException(String message, String reservoirId, double headMeters,
                                       double currentVolumeM3, double reserveThresholdM3) {
        super(message, reservoirId, headMeters / 10.197); // Convert head meters to bar
        this.currentVolumeM3 = currentVolumeM3;
        this.reserveThresholdM3 = reserveThresholdM3;
    }

    public double getCurrentVolumeM3() {
        return currentVolumeM3;
    }

    public double getReserveThresholdM3() {
        return reserveThresholdM3;
    }
}
