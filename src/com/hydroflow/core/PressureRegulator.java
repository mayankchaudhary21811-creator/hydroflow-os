package com.hydroflow.core;

/**
 * Operational contract for pressure regulating elements and surge mitigation devices.
 */
public interface PressureRegulator {
    double getInletPressureBar();
    double getOutletPressureBar();
    double getPressureReductionRatio();
}
