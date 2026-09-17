package com.hydroflow.pump;

/**
 * Models standard fixed-RPM electric centrifugal pumps.
 * Follows classic parabolic head-flow curve H = H0 - A * Q^2.
 */
public class FixedSpeedCentrifugalPump extends Pump {
    private final double shutoffHeadMeters;

    public FixedSpeedCentrifugalPump(String pumpId, String stationName, double ratedFlowLps,
                                     double ratedHeadMeters, double nominalPowerKw) {
        super(pumpId, stationName, ratedFlowLps, ratedHeadMeters, nominalPowerKw, 0.76);
        this.shutoffHeadMeters = ratedHeadMeters * 1.25; // 25% higher head at zero discharge
    }

    @Override
    public double calculateElectricalPowerKw(double flowLps, double targetHeadMeters) {
        if (flowLps <= 0.0) {
            return nominalPowerKw * 0.35; // No-load churning losses
        }
        // Hydraulic power Ph = (rho * g * Q * H) / 1000 in kW
        // rho = 1000 kg/m3, g = 9.81 m/s2, Q in m3/s, H in meters
        double qM3s = flowLps / 1000.0;
        double hydraulicPowerKw = (1000.0 * 9.81 * qM3s * targetHeadMeters) / 1000.0;

        // Fixed speed pump throttling factor
        double throttlingPenalty = 1.0 + 0.18 * Math.pow((flowLps / ratedFlowLps) - 1.0, 2);
        return (hydraulicPowerKw / pumpEfficiency) * throttlingPenalty;
    }

    @Override
    public double getActivePowerDrawKw() {
        return operational ? calculateElectricalPowerKw(activeFlowLps, activeHeadMeters) : 0.0;
    }

    @Override
    public String getPumpTechnology() {
        return "FIXED_SPEED_CENTRIFUGAL";
    }
}
