package com.iudigital.emergencias.medicas;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class Emergency implements Comparable<Emergency> {
    private static final AtomicLong SEQ = new AtomicLong(0);

    public enum Severity { CRITICAL, SERIOUS, MODERATE, MINOR }

    private final long id;
    private final Severity severity;
    private final Instant reportedAt;
    private final double distanceKm; // distancia al centro o a ambulancia
    private final String location;
    private volatile boolean assigned = false;

    public Emergency(Severity severity, String location, double distanceKm) {
        this.id = SEQ.incrementAndGet();
        this.severity = severity;
        this.reportedAt = Instant.now();
        this.location = location;
        this.distanceKm = distanceKm;
    }

    public long getId() { return id; }
    public Severity getSeverity() { return severity; }
    public Instant getReportedAt() { return reportedAt; }
    public double getDistanceKm() { return distanceKm; }
    public String getLocation() { return location; }

    public synchronized boolean isAssigned() { return assigned; }
    public synchronized void setAssigned(boolean assigned) { this.assigned = assigned; }

    // Prioridad natural: gravedad -> tiempo de espera (más antiguo primero) -> distancia (más cercano primero)
    @Override
    public int compareTo(Emergency o) {
        // gravedad
        int s = severityRank(o.severity) - severityRank(this.severity); // CRITICAL should be "less" to be head
        if (s != 0) return s;
        // tiempo de espera (más antiguo con mayor prioridad)
        int t = this.reportedAt.compareTo(o.reportedAt);
        if (t != 0) return t;
        // distancia (menor distancia más prioridad)
        return Double.compare(this.distanceKm, o.distanceKm);
    }

    private int severityRank(Severity sev) {
        return switch (sev) {
            case CRITICAL -> 4;
            case SERIOUS -> 3;
            case MODERATE -> 2;
            default -> 1;
        };
    }

    @Override
    public String toString() {
        return String.format("EMG[id=%d,sev=%s,loc=%s,dist=%.2fkm,reported=%s,assigned=%s]",
                id, severity, location, distanceKm, reportedAt.toString(), assigned);
    }
}
