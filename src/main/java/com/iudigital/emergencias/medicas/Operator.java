package com.iudigital.emergencias.medicas;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Operator implements Runnable {
    private final BlockingQueue<Emergency> queue;
    private final Random random = new Random();
    private final String name;
    private volatile boolean running = true;

    public Operator(String name, BlockingQueue<Emergency> queue) {
        this.name = name;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                // simular llamada entrante
                Emergency.Severity sev = pickSeverity();
                Emergency em = new Emergency(sev, randomLocation(), pickDistance());
                queue.put(em);
                System.out.println("[" + name + "] Nueva llamada: " + em);
                // intervalo aleatorio entre llamadas (100ms - 2s)
                TimeUnit.MILLISECONDS.sleep(200 + random.nextInt(1800));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(name + " interrumpido.");
        }
    }

    private Emergency.Severity pickSeverity() {
        int r = random.nextInt(100);
        if (r < 10) return Emergency.Severity.CRITICAL;
        if (r < 30) return Emergency.Severity.SERIOUS;
        if (r < 70) return Emergency.Severity.MODERATE;
        return Emergency.Severity.MINOR;
    }

    private String randomLocation() { return "Loc-" + (random.nextInt(20) + 1); }
    private double pickDistance() { return Math.round(random.nextDouble() * 20.0 * 100.0) / 100.0; }

    public void shutdown() { running = false; }
}

