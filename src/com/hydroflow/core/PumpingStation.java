package com.hydroflow.core;

import com.hydroflow.pump.Pump;
import com.hydroflow.exceptions.CavitationRiskException;
import java.util.ArrayList;
import java.util.List;

/**
 * Models a municipal booster pumping station containing multiple parallel pump trains.
 * Implements synchronized monitor locking to prevent concurrent electrical grid overload.
 */
public class PumpingStation {
    private final String stationId;
    private final String stationName;
    private final double transformerCapacityKw;
    private final List<Pump> installedPumps;

    public PumpingStation(String stationId, String stationName, double transformerCapacityKw) {
        this.stationId = stationId;
        this.stationName = stationName;
        this.transformerCapacityKw = transformerCapacityKw;
        this.installedPumps = new ArrayList<Pump>();
    }

    public String getStationId() {
        return stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public double getTransformerCapacityKw() {
        return transformerCapacityKw;
    }

    public synchronized void addPump(Pump pump) {
        installedPumps.add(pump);
    }

    public List<Pump> getInstalledPumps() {
        return installedPumps;
    }

    public synchronized double getTotalActivePowerDrawKw() {
        double total = 0.0;
        for (Pump p : installedPumps) {
            total += p.getActivePowerDrawKw();
        }
        return total;
    }

    public synchronized boolean activatePump(String pumpId, double suctionPressureBar,
                                            double targetFlowLps, double targetHeadMeters)
            throws CavitationRiskException {
        for (Pump p : installedPumps) {
            if (p.getActuatorId().equals(pumpId)) {
                // Check NPSH suction boundary
                p.verifySuctionPressure(suctionPressureBar, 3.5); // 3.5 meters required NPSH

                double projectedDraw = p.calculateElectricalPowerKw(targetFlowLps, targetHeadMeters);
                if (getTotalActivePowerDrawKw() + projectedDraw > transformerCapacityKw) {
                    return false; // Grid transformer overload avoided
                }
                p.setOperational(true);
                return true;
            }
        }
        return false;
    }

    public synchronized void shutdownAllPumps() {
        for (Pump p : installedPumps) {
            p.setOperational(false);
        }
    }
}
