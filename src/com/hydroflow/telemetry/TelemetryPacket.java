package com.hydroflow.telemetry;

/**
 * Immutable telematics data carrier for pipeline sensors and pressure transducers.
 */
public class TelemetryPacket {
    private final long timestamp;
    private final String sensorNodeId;
    private final double pressureBar;
    private final double flowRateLps;
    private final double waterTurbidityNTU;
    private final double chlorinePpm;

    public TelemetryPacket(String sensorNodeId, double pressureBar, double flowRateLps,
                           double waterTurbidityNTU, double chlorinePpm) {
        this.timestamp = System.currentTimeMillis();
        this.sensorNodeId = sensorNodeId;
        this.pressureBar = pressureBar;
        this.flowRateLps = flowRateLps;
        this.waterTurbidityNTU = waterTurbidityNTU;
        this.chlorinePpm = chlorinePpm;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSensorNodeId() {
        return sensorNodeId;
    }

    public double getPressureBar() {
        return pressureBar;
    }

    public double getFlowRateLps() {
        return flowRateLps;
    }

    public double getWaterTurbidityNTU() {
        return waterTurbidityNTU;
    }

    public double getChlorinePpm() {
        return chlorinePpm;
    }

    @Override
    public String toString() {
        return String.format("[%d] Node: %s | P: %.2f bar | Q: %.1f L/s | Turbidity: %.2f NTU | Cl: %.2f ppm",
                timestamp, sensorNodeId, pressureBar, flowRateLps, waterTurbidityNTU, chlorinePpm);
    }
}
