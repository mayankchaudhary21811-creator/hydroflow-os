package com.hydroflow.telemetry;

import java.util.Random;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Asynchronous background worker implementing Runnable.
 * Simulates high-frequency remote sensor telemetry streaming without blocking the UI.
 */
public class PressureSensorTelemetryWorker implements Runnable {
    private final String nodeId;
    private final double baselinePressureBar;
    private final int cycleIterations;
    private final List<TelemetryPacket> recordedPackets;
    private volatile boolean running = true;
    private final Random random = new Random(42);

    public PressureSensorTelemetryWorker(String nodeId, double baselinePressureBar, int cycleIterations) {
        this.nodeId = nodeId;
        this.baselinePressureBar = baselinePressureBar;
        this.cycleIterations = cycleIterations;
        this.recordedPackets = new CopyOnWriteArrayList<TelemetryPacket>();
    }

    @Override
    public void run() {
        for (int i = 0; i < cycleIterations && running; i++) {
            // Add slight stochastic measurement noise (+/- 0.25 bar)
            double noise = (random.nextDouble() - 0.5) * 0.5;
            double currentPressure = Math.max(0.5, baselinePressureBar + noise);
            double currentFlow = 45.0 + random.nextDouble() * 15.0;
            double turbidity = 0.4 + random.nextDouble() * 0.3;
            double chlorine = 1.2 + random.nextDouble() * 0.2;

            TelemetryPacket packet = new TelemetryPacket(nodeId, currentPressure, currentFlow, turbidity, chlorine);
            recordedPackets.add(packet);

            try {
                Thread.sleep(40); // 40ms simulation sample interval
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stopWorker() {
        this.running = false;
    }

    public List<TelemetryPacket> getRecordedPackets() {
        return recordedPackets;
    }
}
