package com.hydroflow.core;

import com.hydroflow.exceptions.PipeBurstSurgeException;

/**
 * Models a ductile iron or high-density polyethylene pipeline segment.
 * Implements Hazen-Williams head loss and Joukowsky transient surge formulations.
 */
public class DistributionPipeline implements Auditable {
    private final String pipeId;
    private final String upstreamNodeId;
    private final String downstreamNodeId;
    private final double lengthMeters;
    private final double diameterMm;
    private final double roughnessC; // Hazen-Williams coefficient (e.g. 130 for ductile iron)
    private final double maxPressureRatingBar; // e.g. PN16 = 16.0 bar
    private double currentFlowLps;

    public DistributionPipeline(String pipeId, String upstreamNodeId, String downstreamNodeId,
                                double lengthMeters, double diameterMm, double roughnessC,
                                double maxPressureRatingBar) {
        this.pipeId = pipeId;
        this.upstreamNodeId = upstreamNodeId;
        this.downstreamNodeId = downstreamNodeId;
        this.lengthMeters = lengthMeters;
        this.diameterMm = diameterMm;
        this.roughnessC = roughnessC;
        this.maxPressureRatingBar = maxPressureRatingBar;
        this.currentFlowLps = 0.0;
    }

    public String getPipeId() {
        return pipeId;
    }

    public double getLengthMeters() {
        return lengthMeters;
    }

    public double getDiameterMm() {
        return diameterMm;
    }

    public double getCurrentFlowLps() {
        return currentFlowLps;
    }

    public void setCurrentFlowLps(double currentFlowLps) {
        this.currentFlowLps = currentFlowLps;
    }

    public double getMaxPressureRatingBar() {
        return maxPressureRatingBar;
    }

    /**
     * Calculates hydraulic friction head loss via Hazen-Williams empirical equation:
     * h_f = 10.67 * L * (Q^1.852) / (C^1.852 * D^4.87)
     * Q in m3/s, D in meters, L in meters
     */
    public double calculateHeadLossMeters(double flowLps) {
        if (flowLps <= 0.0) {
            return 0.0;
        }
        double qM3s = flowLps / 1000.0;
        double dMeters = diameterMm / 1000.0;

        double numerator = 10.67 * lengthMeters * Math.pow(qM3s, 1.852);
        double denominator = Math.pow(roughnessC, 1.852) * Math.pow(dMeters, 4.87);

        return numerator / denominator;
    }

    /**
     * Calculates transient water hammer surge pressure upon rapid valve closure using Joukowsky formula:
     * delta_P (Pa) = rho * a * delta_v
     * delta_P (bar) = (rho * a * delta_v) / 100,000
     */
    public void verifyTransientSurgeSafety(double flowChangeLps, double initialPressureBar)
            throws PipeBurstSurgeException {
        double dMeters = diameterMm / 1000.0;
        double area = Math.PI * Math.pow(dMeters / 2.0, 2);
        double deltaV = (flowChangeLps / 1000.0) / area; // velocity change in m/s

        double waterDensity = 1000.0; // kg/m3
        double waveSpeed = 1050.0;    // acoustic pressure wave velocity in water-filled pipe (m/s)

        double surgePressurePa = waterDensity * waveSpeed * deltaV;
        double surgePressureBar = surgePressurePa / 100000.0;
        double totalPeakPressureBar = initialPressureBar + surgePressureBar;

        if (totalPeakPressureBar > maxPressureRatingBar) {
            throw new PipeBurstSurgeException(
                "Water hammer transient exceeds pipe pressure rating; automatic surge valve dump required.",
                pipeId,
                totalPeakPressureBar,
                maxPressureRatingBar
            );
        }
    }

    @Override
    public String toCsvRecord() {
        return String.format("%s,%s,%s,%.1f,%.1f,%.1f,%.2f,%.1f",
                pipeId, upstreamNodeId, downstreamNodeId, lengthMeters, diameterMm, roughnessC,
                currentFlowLps, maxPressureRatingBar);
    }

    @Override
    public String getAuditSummary() {
        return String.format("Pipe [%s] Flow: %.2f L/s | Friction Loss: %.3f m head | Rating: %.1f bar",
                pipeId, currentFlowLps, calculateHeadLossMeters(currentFlowLps), maxPressureRatingBar);
    }
}
