package com.hydroflow.core;

/**
 * Contract for network entities that export structured audit strings and CSV records.
 */
public interface Auditable {
    String toCsvRecord();
    String getAuditSummary();
}
