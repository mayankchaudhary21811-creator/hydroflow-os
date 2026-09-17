package com.hydroflow.exceptions;

/**
 * Root checked exception for hydraulic distribution boundary conditions.
 * Encapsulates timestamped diagnostic telemetry for caller catch blocks.
 */
public class HydroFlowException extends Exception {
    private final String componentId;
    private final double observedPressureBar;
    private final long timestampMillis;

    public HydroFlowException(String message, String componentId, double observedPressureBar) {
        super(message);
        this.componentId = componentId;
        this.observedPressureBar = observedPressureBar;
        this.timestampMillis = System.currentTimeMillis();
    }

    public String getComponentId() {
        return componentId;
    }

    public double getObservedPressureBar() {
        return observedPressureBar;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    @Override
    public String getMessage() {
        return String.format("%s [Component: %s, Pressure: %.2f bar, Time: %d]",
                super.getMessage(), componentId, observedPressureBar, timestampMillis);
    }
}
