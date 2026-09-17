package com.hydroflow.core;

import com.hydroflow.exceptions.ReservoirDepletionException;

/**
 * Concrete network node modeling elevated storage tanks and municipal water reservoirs.
 * Implements Comparable to prioritize refilling queues by lowest volumetric fill percentage.
 */
public class ReservoirTank extends WaterNetworkNode implements Comparable<ReservoirTank> {
    private final double maxCapacityM3;
    private double currentVolumeM3;
    private final double emergencyReserveRatio = 0.15; // 15% safety buffer

    public ReservoirTank(String nodeId, String zoneName, double elevationMeters,
                         double maxCapacityM3, double currentVolumeM3) {
        super(nodeId, zoneName, elevationMeters, (currentVolumeM3 / maxCapacityM3) * 30.0, 0.0);
        this.maxCapacityM3 = maxCapacityM3;
        this.currentVolumeM3 = currentVolumeM3;
    }

    public double getMaxCapacityM3() {
        return maxCapacityM3;
    }

    public synchronized double getCurrentVolumeM3() {
        return currentVolumeM3;
    }

    public synchronized double getFillPercentage() {
        return (currentVolumeM3 / maxCapacityM3) * 100.0;
    }

    public synchronized void dischargeWater(double volumeM3) throws ReservoirDepletionException {
        double safeReserveM3 = maxCapacityM3 * emergencyReserveRatio;
        if (currentVolumeM3 - volumeM3 < safeReserveM3) {
            throw new ReservoirDepletionException(
                "Refusing discharge: Reservoir volume drops below 15% emergency reserve threshold.",
                nodeId,
                pressureHeadMeters,
                currentVolumeM3 - volumeM3,
                safeReserveM3
            );
        }
        this.currentVolumeM3 -= volumeM3;
        this.pressureHeadMeters = (this.currentVolumeM3 / maxCapacityM3) * 30.0;
    }

    public synchronized void replenishWater(double volumeM3) {
        this.currentVolumeM3 = Math.min(maxCapacityM3, this.currentVolumeM3 + volumeM3);
        this.pressureHeadMeters = (this.currentVolumeM3 / maxCapacityM3) * 30.0;
    }

    @Override
    public String getNodeType() {
        return "ELEVATED_RESERVOIR";
    }

    @Override
    public int compareTo(ReservoirTank other) {
        // PriorityQueue order: lowest fill percentage first
        return Double.compare(this.getFillPercentage(), other.getFillPercentage());
    }

    @Override
    public String toCsvRecord() {
        return String.format("%s,%s,%s,%.2f,%.2f,%.2f,%.1f%%",
                nodeId, zoneName, getNodeType(), elevationMeters, currentVolumeM3, maxCapacityM3, getFillPercentage());
    }

    @Override
    public String getAuditSummary() {
        return String.format("Reservoir [%s] Fill: %.1f%% (%.1f / %.1f m3)",
                nodeId, getFillPercentage(), currentVolumeM3, maxCapacityM3);
    }
}
