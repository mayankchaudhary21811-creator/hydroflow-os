package com.hydroflow.exceptions;

/**
 * Thrown when Net Positive Suction Head Available (NPSHa) drops below
 * the pump Net Positive Suction Head Required (NPSHr), risking vapor bubble collapse.
 */
public class CavitationRiskException extends HydroFlowException {
    private final double npshAvailable;
    private final double npshRequired;

    public CavitationRiskException(String message, String pumpId, double suctionPressureBar,
                                   double npshAvailable, double npshRequired) {
        super(message, pumpId, suctionPressureBar);
        this.npshAvailable = npshAvailable;
        this.npshRequired = npshRequired;
    }

    public double getNpshAvailable() {
        return npshAvailable;
    }

    public double getNpshRequired() {
        return npshRequired;
    }
}
