package com.hydroflow.pump;

import com.hydroflow.core.HydraulicActuator;
import com.hydroflow.core.Auditable;
import com.hydroflow.exceptions.CavitationRiskException;

/**
 * Abstract base class modeling industrial pumping units in municipal booster stations.
 * Demonstrates runtime dynamic method dispatch across varying motor-impeller physics models.
 */
public abstract class Pump implements HydraulicActuator, Auditable {
    protected final String pumpId;
    protected final String stationName;
    protected final double ratedFlowLps;
    protected final double ratedHeadMeters;
    protected final double nominalPowerKw;
    protected final double pumpEfficiency; // e.g. 0.78 for 78% mechanical/hydraulic efficiency
    protected boolean operational;
    protected double activeFlowLps;
    protected double activeHeadMeters;

    public Pump(String pumpId, String stationName, double ratedFlowLps, double ratedHeadMeters,
                double nominalPowerKw, double pumpEfficiency) {
        this.pumpId = pumpId;
        this.stationName = stationName;
        this.ratedFlowLps = ratedFlowLps;
        this.ratedHeadMeters = ratedHeadMeters;
        this.nominalPowerKw = nominalPowerKw;
        this.pumpEfficiency = pumpEfficiency;
        this.operational = false;
        this.activeFlowLps = 0.0;
        this.activeHeadMeters = 0.0;
    }

    @Override
    public String getActuatorId() {
        return pumpId;
    }

    @Override
    public boolean isOperational() {
        return operational;
    }

    @Override
    public void setOperational(boolean operational) {
        this.operational = operational;
    }

    public String getStationName() {
        return stationName;
    }

    public double getRatedFlowLps() {
        return ratedFlowLps;
    }

    public double getRatedHeadMeters() {
        return ratedHeadMeters;
    }

    public double getActiveFlowLps() {
        return activeFlowLps;
    }

    public double getActiveHeadMeters() {
        return activeHeadMeters;
    }

    /**
     * Verifies that suction pressure prevents cavitation damage.
     */
    public void verifySuctionPressure(double suctionPressureBar, double requiredNpshMeters)
            throws CavitationRiskException {
        double availableNpshMeters = suctionPressureBar * 10.197;
        if (availableNpshMeters < requiredNpshMeters) {
            throw new CavitationRiskException(
                "Suction pressure insufficient: Net positive suction head margin breached.",
                pumpId,
                suctionPressureBar,
                availableNpshMeters,
                requiredNpshMeters
            );
        }
    }

    /**
     * Polymorphic method overridden by subclasses to calculate electrical power draw
     * using specific impeller characteristics and speed regulation principles.
     */
    public abstract double calculateElectricalPowerKw(double flowLps, double targetHeadMeters);

    public abstract String getPumpTechnology();

    @Override
    public String toCsvRecord() {
        return String.format("%s,%s,%s,%.1f,%.1f,%.2f,%.2f,%b",
                pumpId, stationName, getPumpTechnology(), ratedFlowLps, ratedHeadMeters,
                nominalPowerKw, pumpEfficiency, operational);
    }

    @Override
    public String getAuditSummary() {
        return String.format("Pump [%s] Type: %s | State: %s | Flow: %.1f L/s | Head: %.1f m",
                pumpId, getPumpTechnology(), (operational ? "RUNNING" : "STOPPED"),
                activeFlowLps, activeHeadMeters);
    }
}
