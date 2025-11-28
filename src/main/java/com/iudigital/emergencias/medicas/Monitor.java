package com.iudigital.emergencias.medicas;

import java.util.concurrent.*;

public class Monitor {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final BlockingQueue<Emergency> queue;
    private final ResourceManager rm = ResourceManager.getInstance();

    public Monitor(BlockingQueue<Emergency> queue) {
        this.queue = queue;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::report, 0, 2, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    private void report() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("---- MONITOR ----\n");
            sb.append("Pending emergencies: ").append(queue.size()).append("\n");
            for (Emergency e : queue) {
                sb.append("  ").append(e).append("\n");
            }
            sb.append(rm.status()).append("\n");
            System.out.println(sb.toString());
        } catch (Exception ex) {
            System.err.println("Monitor error: " + ex.getMessage());
        }
    }
}

