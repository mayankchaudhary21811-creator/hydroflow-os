package com.hydroflow.test;

import com.hydroflow.core.ReservoirTank;
import com.hydroflow.core.DistributionPipeline;
import com.hydroflow.core.PumpingStation;
import com.hydroflow.pump.FixedSpeedCentrifugalPump;
import com.hydroflow.pump.VariableFrequencyDrivePump;
import com.hydroflow.exceptions.InvalidNodeIdentifierException;
import com.hydroflow.exceptions.ReservoirDepletionException;
import com.hydroflow.exceptions.PipeBurstSurgeException;
import com.hydroflow.exceptions.CavitationRiskException;
import com.hydroflow.telemetry.PressureSensorTelemetryWorker;
import com.hydroflow.io.NetworkPersistenceManager;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Self-contained unit test suite executing with native Java assertions (-ea).
 */
public class HydroFlowTestSuite {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("              RUNNING HYDROFLOW OS UNIT TEST SUITE               ");
        System.out.println("=================================================================");

        int passed = 0;
        int total = 6;

        try {
            testNodeIdentifierValidation();
            System.out.println("  [PASS] testNodeIdentifierValidation          ... OK");
            passed++;
        } catch (Throwable t) {
            System.out.println("  [FAIL] testNodeIdentifierValidation          : " + t.getMessage());
        }

        try {
            testPolymorphicPowerCalculation();
            System.out.println("  [PASS] testPolymorphicPowerCalculation         ... OK");
            passed++;
        } catch (Throwable t) {
            System.out.println("  [FAIL] testPolymorphicPowerCalculation         : " + t.getMessage());
        }

        try {
            testReservoirDepletionInterlock();
            System.out.println("  [PASS] testReservoirDepletionInterlock         ... OK");
            passed++;
        } catch (Throwable t) {
            System.out.println("  [FAIL] testReservoirDepletionInterlock         : " + t.getMessage());
        }

        try {
            testWaterHammerSurgeProtection();
            System.out.println("  [PASS] testWaterHammerSurgeProtection          ... OK");
            passed++;
        } catch (Throwable t) {
            System.out.println("  [FAIL] testWaterHammerSurgeProtection          : " + t.getMessage());
        }

        try {
            testMultithreadedSensorWorker();
            System.out.println("  [PASS] testMultithreadedSensorWorker           ... OK");
            passed++;
        } catch (Throwable t) {
            System.out.println("  [FAIL] testMultithreadedSensorWorker           : " + t.getMessage());
        }

        try {
            testCsvPersistenceRoundtrip();
            System.out.println("  [PASS] testCsvPersistenceRoundtrip             ... OK");
            passed++;
        } catch (Throwable t) {
            System.out.println("  [FAIL] testCsvPersistenceRoundtrip             : " + t.getMessage());
        }

        System.out.println("=================================================================");
        System.out.printf("TEST RESULTS: %d / %d PASSED (Success Rate: %.1f%%)\n",
                passed, total, (passed * 100.0 / total));
        System.out.println("=================================================================");

        if (passed != total) {
            System.exit(1);
        }
    }

    private static void testNodeIdentifierValidation() {
        // Valid identifier
        ReservoirTank valid = new ReservoirTank("RESNODE01", "Zone-A", 100.0, 1000.0, 800.0);
        if (!valid.getNodeId().equals("RESNODE01")) {
            throw new AssertionError("Node ID mismatch");
        }

        // Invalid identifier (contains special characters)
        boolean caught = false;
        try {
            new ReservoirTank("INVALID#NODE", "Zone-A", 100.0, 1000.0, 800.0);
        } catch (InvalidNodeIdentifierException e) {
            caught = true;
        }
        if (!caught) {
            throw new AssertionError("Failed to reject invalid node identifier pattern");
        }
    }

    private static void testPolymorphicPowerCalculation() {
        FixedSpeedCentrifugalPump fixed = new FixedSpeedCentrifugalPump("P1", "Stn1", 50.0, 40.0, 45.0);
        VariableFrequencyDrivePump vfd = new VariableFrequencyDrivePump("P2", "Stn1", 50.0, 40.0, 45.0);

        double powerFixed = fixed.calculateElectricalPowerKw(40.0, 35.0);
        double powerVfd = vfd.calculateElectricalPowerKw(40.0, 35.0);

        // VFD must consume less power than fixed-speed pump at partial flow
        if (powerVfd >= powerFixed) {
            throw new AssertionError("VFD pump should consume less power at partial flow");
        }
    }

    private static void testReservoirDepletionInterlock() throws ReservoirDepletionException {
        ReservoirTank tank = new ReservoirTank("TANK001", "Zone-B", 50.0, 1000.0, 500.0);
        // Discharging 300 m3 leaves 200 m3 (20% > 15% reserve) -> OK
        tank.dischargeWater(300.0);

        // Discharging another 100 m3 leaves 100 m3 (10% < 15% reserve) -> Must throw
        boolean blocked = false;
        try {
            tank.dischargeWater(100.0);
        } catch (ReservoirDepletionException e) {
            blocked = true;
        }
        if (!blocked) {
            throw new AssertionError("Reservoir allowed discharge breaching emergency reserve");
        }
    }

    private static void testWaterHammerSurgeProtection() {
        DistributionPipeline pipe = new DistributionPipeline("PIPE01", "N1", "N2", 1000.0, 300.0, 130.0, 10.0);
        boolean caught = false;
        try {
            // High velocity change triggers pressure burst above 10.0 bar rating
            pipe.verifyTransientSurgeSafety(120.0, 6.0);
        } catch (PipeBurstSurgeException e) {
            caught = true;
        }
        if (!caught) {
            throw new AssertionError("Failed to detect water hammer pressure surge rating breach");
        }
    }

    private static void testMultithreadedSensorWorker() throws InterruptedException {
        PressureSensorTelemetryWorker worker = new PressureSensorTelemetryWorker("NODE-SENS-1", 4.0, 6);
        Thread t = new Thread(worker);
        t.start();
        t.join(3000);

        if (worker.getRecordedPackets().isEmpty()) {
            throw new AssertionError("Worker thread produced no telemetry packets");
        }
    }

    private static void testCsvPersistenceRoundtrip() throws Exception {
        File temp = File.createTempFile("hydroflow_test_", ".csv");
        temp.deleteOnExit();

        ReservoirTank tank = new ReservoirTank("TESTRES01", "Zone-Test", 80.0, 2000.0, 1500.0);
        NetworkPersistenceManager.exportReservoirsToCsv(Collections.singletonList(tank), temp);

        List<String> lines = NetworkPersistenceManager.readReservoirCsvLines(temp);
        if (lines.size() != 1 || !lines.get(0).contains("TESTRES01")) {
            throw new AssertionError("CSV persistence roundtrip data corrupted");
        }
    }
}
