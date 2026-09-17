package com.hydroflow.io;

import com.hydroflow.core.ReservoirTank;
import com.hydroflow.core.DistributionPipeline;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles structured file persistence using BufferedReader and BufferedWriter.
 * Wraps file handles in try-with-resources blocks for safe descriptor management.
 */
public class NetworkPersistenceManager {

    public static void exportReservoirsToCsv(List<ReservoirTank> reservoirs, File destination)
            throws IOException {
        File parent = destination.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(destination))) {
            writer.write("NodeID,Zone,Type,ElevationMeters,CurrentVolumeM3,MaxCapacityM3,FillLevel");
            writer.newLine();
            for (ReservoirTank r : reservoirs) {
                writer.write(r.toCsvRecord());
                writer.newLine();
            }
        }
    }

    public static List<String> readReservoirCsvLines(File source) throws IOException {
        List<String> records = new ArrayList<String>();
        if (!source.exists()) {
            return records;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(source))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                if (!line.trim().isEmpty()) {
                    records.add(line.trim());
                }
            }
        }
        return records;
    }

    public static void appendOperationalEvent(File auditLog, String eventMessage) throws IOException {
        File parent = auditLog.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(auditLog, true))) {
            writer.write(String.format("[%d] %s", System.currentTimeMillis(), eventMessage));
            writer.newLine();
        }
    }
}
