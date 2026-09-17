package com.hydroflow.core;

import com.hydroflow.exceptions.InvalidNodeIdentifierException;
import java.util.regex.Pattern;

/**
 * Abstract base class modeling a physical junction node within a municipal hydraulic distribution grid.
 */
public abstract class WaterNetworkNode implements Auditable {
    private static final Pattern NODE_ID_PATTERN = Pattern.compile("^[A-Z0-9]{6,16}$");

    protected final String nodeId;
    protected final String zoneName;
    protected final double elevationMeters;
    protected double pressureHeadMeters;
    protected double baseDemandLps;

    public WaterNetworkNode(String nodeId, String zoneName, double elevationMeters,
                            double pressureHeadMeters, double baseDemandLps) {
        if (nodeId == null || !NODE_ID_PATTERN.matcher(nodeId).matches()) {
            throw new InvalidNodeIdentifierException(nodeId);
        }
        this.nodeId = nodeId;
        this.zoneName = zoneName;
        this.elevationMeters = elevationMeters;
        this.pressureHeadMeters = pressureHeadMeters;
        this.baseDemandLps = baseDemandLps;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public double getElevationMeters() {
        return elevationMeters;
    }

    public double getPressureHeadMeters() {
        return pressureHeadMeters;
    }

    public void setPressureHeadMeters(double pressureHeadMeters) {
        this.pressureHeadMeters = pressureHeadMeters;
    }

    public double getPressureBar() {
        // 1 bar approx equals 10.197 meters of water column head
        return this.pressureHeadMeters / 10.197;
    }

    public double getBaseDemandLps() {
        return baseDemandLps;
    }

    public void setBaseDemandLps(double baseDemandLps) {
        this.baseDemandLps = baseDemandLps;
    }

    public abstract String getNodeType();
}
