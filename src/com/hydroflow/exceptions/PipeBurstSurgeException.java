package com.hydroflow.exceptions;

/**
 * Thrown when rapid valve closure or sudden pump trip generates a Joukowsky
 * water hammer pressure transient exceeding maximum allowable pipe pressure (PN rating).
 */
public class PipeBurstSurgeException extends HydroFlowException {
    private final double transientSurgeBar;
    private final double pipePressureRatingBar;

    public PipeBurstSurgeException(String message, String pipelineId, double transientSurgeBar,
                                  double pipePressureRatingBar) {
        super(message, pipelineId, transientSurgeBar);
        this.transientSurgeBar = transientSurgeBar;
        this.pipePressureRatingBar = pipePressureRatingBar;
    }

    public double getTransientSurgeBar() {
        return transientSurgeBar;
    }

    public double getPipePressureRatingBar() {
        return pipePressureRatingBar;
    }
}
