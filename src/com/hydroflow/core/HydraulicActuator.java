package com.hydroflow.core;

/**
 * Operational contract for electro-mechanical network actuators (pumps, motorized valves).
 */
public interface HydraulicActuator {
    String getActuatorId();
    boolean isOperational();
    void setOperational(boolean operational);
    double getActivePowerDrawKw();
}
