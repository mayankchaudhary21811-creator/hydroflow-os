package com.hydroflow.pump;

/**
 * Models deep-well and in-line multi-stage submersible booster pumps designed
 * for elevated high-pressure distribution zones.
 */
public class SubmersibleBoosterPump extends Pump {
    private final int impellerStages;

    public SubmersibleBoosterPump(String pumpId, String stationName, double ratedFlowLps,
                                  double ratedHeadMeters, double nominalPowerKw, int impellerStages) {
        super(pumpId, stationName, ratedFlowLps, ratedHeadMeters, nominalPowerKw, 0.79);
        this.impellerStages = impellerStages;
    }

    public int getImpellerStages() {
        return impellerStages;
    }

    @Override
    public double calculateElectricalPowerKw(double flowLps, double targetHeadMeters) {
        if (flowLps <= 0.0) {
            return nominalPowerKw * 0.28;
        }
        double qM3s = flowLps / 1000.0;
        double hydraulicPowerKw = (1000.0 * 9.81 * qM3s * targetHeadMeters) / 1000.0;

        // Multi-stage friction factor adds 2% per extra stage
        double stagePenalty = 1.0 + (0.015 * (impellerStages - 1));
        return (hydraulicPowerKw / pumpEfficiency) * stagePenalty;
    }

    @Override
    public double getActivePowerDrawKw() {
        return operational ? calculateElectricalPowerKw(activeFlowLps, activeHeadMeters) : 0.0;
    }

    @Override
    public String getPumpTechnology() {
        return "SUBMERSIBLE_BOOSTER";
    }
}
