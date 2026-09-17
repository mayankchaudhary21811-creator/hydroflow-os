package com.hydroflow.cli;

import com.hydroflow.core.ReservoirTank;
import com.hydroflow.core.DistributionPipeline;
import com.hydroflow.core.PumpingStation;
import com.hydroflow.pump.FixedSpeedCentrifugalPump;
import com.hydroflow.pump.VariableFrequencyDrivePump;
import com.hydroflow.pump.SubmersibleBoosterPump;
import com.hydroflow.telemetry.PressureSensorTelemetryWorker;
import com.hydroflow.exceptions.ReservoirDepletionException;
import com.hydroflow.exceptions.PipeBurstSurgeException;
import com.hydroflow.exceptions.CavitationRiskException;
import com.hydroflow.io.NetworkPersistenceManager;

import java.io.File;
import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * Main application console providing interactive commands and automated evaluation traces.
 */
public class HydroFlowApp {

    public static void main(String[] args) {
        if (args.length > 0 && "--demo".equalsIgnoreCase(args[0])) {
            runAutomatedEvaluation();
            return;
        }
        printBanner();
        System.out.println("Running default evaluation demonstration...");
        runAutomatedEvaluation();
    }

    public static void printBanner() {
        System.out.println("================================================================================");
        System.out.println("   HYDROFLOW OS: MUNICIPAL WATER DISTRIBUTION & SURGE PROTECTION ENGINE         ");
        System.out.println("   Evaluated Course Project | CSE2006 Programming in Java | VIT Bhopal          ");
        System.out.println("   Student: Mayank Chaudhary (24BEC10011) | Term: Fall Semester 2026-2027       ");
        System.out.println("================================================================================");
    }

    public static void runAutomatedEvaluation() {
        printBanner();

        System.out.println("\n>>> [PHASE 1: NETWORK TOPOLOGY INITIALIZATION]");
        ReservoirTank resCentral = new ReservoirTank("RESN01CENTRAL", "Zone-Central", 145.0, 5000.0, 4200.0);
        ReservoirTank resElevNorth = new ReservoirTank("RESN02NORTH", "Zone-NorthHill", 220.0, 3000.0, 650.0); // Low SoC (21.7%)
        ReservoirTank resIndustrial = new ReservoirTank("RESN03INDUS", "Zone-Industrial", 110.0, 8000.0, 6800.0);

        System.out.println("Central Reservoir : " + resCentral.getAuditSummary());
        System.out.println("North Hill Tank   : " + resElevNorth.getAuditSummary());
        System.out.println("Industrial Buffer : " + resIndustrial.getAuditSummary());

        // Priority Queue Test
        PriorityQueue<ReservoirTank> refillQueue = new PriorityQueue<ReservoirTank>();
        refillQueue.add(resCentral);
        refillQueue.add(resElevNorth);
        refillQueue.add(resIndustrial);

        System.out.println("\n>>> [PHASE 2: REFILL PRIORITY QUEUE INSPECTION]");
        ReservoirTank highestPriority = refillQueue.peek();
        System.out.printf("Pumping Priority #1: %s with lowest fill (%.1f%%)\n",
                highestPriority.getNodeId(), highestPriority.getFillPercentage());

        // Pipeline Hydraulic Friction & Surge Test
        System.out.println("\n>>> [PHASE 3: PIPELINE HAZEN-WILLIAMS & SURGE EVALUATION]");
        DistributionPipeline trunkLine = new DistributionPipeline("PIPE-TRUNK-01", "RESN01CENTRAL", "NODE-JUNCTION-A",
                1800.0, 400.0, 130.0, 16.0); // 16 bar rating
        double headLoss = trunkLine.calculateHeadLossMeters(85.0);
        System.out.printf("Pipeline %s (L=%.0fm, D=%.0fmm): At 85.0 L/s flow, head loss = %.3f meters\n",
                trunkLine.getPipeId(), trunkLine.getLengthMeters(), trunkLine.getDiameterMm(), headLoss);

        // Water Hammer Transient Verification
        try {
            System.out.println("Testing standard valve closure transient (flow drop: 40 L/s)...");
            trunkLine.verifyTransientSurgeSafety(40.0, 6.2);
            System.out.println("Transient within safe limits: PASS (No rupture risk detected)");
        } catch (PipeBurstSurgeException e) {
            System.out.println("SURGE DETECTED: " + e.getMessage());
        }

        try {
            System.out.println("Testing emergency rapid valve trip transient (flow drop: 160 L/s)...");
            trunkLine.verifyTransientSurgeSafety(160.0, 6.2);
        } catch (PipeBurstSurgeException e) {
            System.out.println("CAUGHT EXPECTED EXCEPTION: " + e.getMessage());
            System.out.println("Surge relief safety bypass actuated successfully.");
        }

        // Booster Station & Dynamic Pump Dispatch
        System.out.println("\n>>> [PHASE 4: MULTI-STAGE PUMP POWER DISPATCH]");
        PumpingStation station1 = new PumpingStation("STN-EAST-01", "Valley Booster Station", 300.0); // 300 kW transformer
        FixedSpeedCentrifugalPump pumpFixed = new FixedSpeedCentrifugalPump("PUMP-FX-01", "Valley Station", 60.0, 45.0, 45.0);
        VariableFrequencyDrivePump pumpVfd = new VariableFrequencyDrivePump("PUMP-VFD-01", "Valley Station", 90.0, 55.0, 75.0);
        SubmersibleBoosterPump pumpBooster = new SubmersibleBoosterPump("PUMP-BST-01", "Valley Station", 40.0, 95.0, 65.0, 4);

        station1.addPump(pumpFixed);
        station1.addPump(pumpVfd);
        station1.addPump(pumpBooster);

        double pFixed = pumpFixed.calculateElectricalPowerKw(55.0, 42.0);
        double pVfd = pumpVfd.calculateElectricalPowerKw(55.0, 42.0);
        double pBooster = pumpBooster.calculateElectricalPowerKw(35.0, 90.0);

        System.out.printf("Fixed Speed Pump Power (55 L/s @ 42m) : %.2f kW\n", pFixed);
        System.out.printf("VFD Pump Power (55 L/s @ 42m)          : %.2f kW (Energy Savings: %.1f%%)\n",
                pVfd, ((pFixed - pVfd) / pFixed) * 100.0);
        System.out.printf("Submersible High-Head Power (35 L/s)   : %.2f kW\n", pBooster);

        // Reservoir Reserve Boundary Protection
        System.out.println("\n>>> [PHASE 5: RESERVOIR EMERGENCY RESERVE INTERLOCK]");
        try {
            System.out.println("Attempting heavy peak draw (3800 m3) from Central Reservoir (Current: 4200 m3)...");
            resCentral.dischargeWater(3800.0);
        } catch (ReservoirDepletionException e) {
            System.out.println("CAUGHT EXPECTED INTERLOCK: " + e.getMessage());
            System.out.println("Reserve interlock engaged: Emergency 15% volume preserved.");
        }

        // Multithreaded Sensor Telemetry
        System.out.println("\n>>> [PHASE 6: MULTITHREADED SENSOR TELEMETRY WORKER]");
        PressureSensorTelemetryWorker worker = new PressureSensorTelemetryWorker("SENS-NODE-77", 4.5, 5);
        Thread sensorThread = new Thread(worker, "HydroTelemetry-Worker");
        sensorThread.start();
        try {
            sensorThread.join();
            System.out.printf("Telemetry worker joined successfully. Generated %d sensor packets:\n",
                    worker.getRecordedPackets().size());
            for (int i = 0; i < Math.min(3, worker.getRecordedPackets().size()); i++) {
                System.out.println("  " + worker.getRecordedPackets().get(i));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // File Persistence
        System.out.println("\n>>> [PHASE 7: STRUCTURED CSV AUDIT PERSISTENCE]");
        File dataDir = new File("data");
        File csvFile = new File(dataDir, "reservoirs.csv");
        File auditFile = new File(dataDir, "audit_events.log");
        try {
            NetworkPersistenceManager.exportReservoirsToCsv(
                Arrays.asList(resCentral, resElevNorth, resIndustrial),
                csvFile
            );
            System.out.println("Exported reservoir inventory to: " + csvFile.getPath());
            NetworkPersistenceManager.appendOperationalEvent(auditFile, "HydroFlow OS automated audit test complete.");
            System.out.println("Appended diagnostic record to: " + auditFile.getPath());
        } catch (Exception e) {
            System.out.println("File persistence notice: " + e.getMessage());
        }

        System.out.println("\n================================================================================");
        System.out.println("                    AUTOMATED EVALUATION DEMO COMPLETE                          ");
        System.out.println("================================================================================");
    }
}
