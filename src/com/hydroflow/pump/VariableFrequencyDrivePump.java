package com.hydroflow.pump;

/**
 * Models variable-frequency drive (VFD) pumps capable of continuous speed adjustment.
 * Applies centrifugal affinity laws to optimize electrical power consumption.
 */
public class VariableFrequencyDrivePump extends Pump {
    private double operationalFrequencyHz; // 30.0 Hz to 60.0 Hz

    public VariableFrequencyDrivePump(String pumpId, String stationName, double ratedFlowLps,
                                      double ratedHeadMeters, double nominalPowerKw) {
        super(pumpId, stationName, ratedFlowLps, ratedHeadMeters, nominalPowerKw, 0.84);
        this.operationalFrequencyHz = 50.0;
    }

    public double getOperationalFrequencyHz() {
        return operationalFrequencyHz;
    }

    public void setOperationalFrequencyHz(double frequencyHz) {
        this.operationalFrequencyHz = Math.max(30.0, Math.min(60.0, frequencyHz));
    }

    @Override
    public double calculateElectricalPowerKw(double flowLps, double targetHeadMeters) {
        if (flowLps <= 0.0) {
            return nominalPowerKw * 0.12; // Lower idle inverter losses
        }
        double qM3s = flowLps / 1000.0;
        double hydraulicPowerKw = (1000.0 * 9.81 * qM3s * targetHeadMeters) / 1000.0;

        // Affinity law scaling: Speed ratio alpha = flow / ratedFlow
        double alpha = Math.max(0.5, Math.min(1.2, flowLps / ratedFlowLps));
        // Power scales with cube of speed alpha^3 under variable frequency control
        double vfdEfficiencyFactor = 0.94; // Inverter harmonic factor
        return (hydraulicPowerKw / (pumpEfficiency * vfdEfficiencyFactor)) * Math.pow(alpha, 0.85);
    }

    @Override
    public double getActivePowerDrawKw() {
        return operational ? calculateElectricalPowerKw(activeFlowLps, activeHeadMeters) : 0.0;
    }

    @Override
    public String getPumpTechnology() {
        return "VARIABLE_FREQUENCY_DRIVE";
    }
}
