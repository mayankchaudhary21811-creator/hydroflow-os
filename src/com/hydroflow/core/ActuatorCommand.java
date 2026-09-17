package com.hydroflow.core;

/**
 * Immutable command record modeling an actuation event for audit logs and rollback stacks.
 */
public class ActuatorCommand {
    private final long timestamp;
    private final String actuatorId;
    private final String commandType;
    private final double previousValue;
    private final double newValue;

    public ActuatorCommand(String actuatorId, String commandType, double previousValue, double newValue) {
        this.timestamp = System.currentTimeMillis();
        this.actuatorId = actuatorId;
        this.commandType = commandType;
        this.previousValue = previousValue;
        this.newValue = newValue;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getActuatorId() {
        return actuatorId;
    }

    public String getCommandType() {
        return commandType;
    }

    public double getPreviousValue() {
        return previousValue;
    }

    public double getNewValue() {
        return newValue;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s on %s: %.2f -> %.2f",
                timestamp, commandType, actuatorId, previousValue, newValue);
    }
}
