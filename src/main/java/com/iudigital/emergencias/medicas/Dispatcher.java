package com.iudigital.emergencias.medicas;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Dispatcher implements Runnable {
    private final BlockingQueue<Emergency> queue;
    private final ResourceManager rm = ResourceManager.getInstance();
    private volatile boolean running = true;
    private final String name;

    public Dispatcher(String name, BlockingQueue<Emergency> queue) {
        this.name = name;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                Emergency em = queue.take(); // bloquea hasta que haya emergencia
                System.out.println("[" + name + "] Procesando: " + em);

                // intentar conseguir ambulancia con timeout
                Optional<Ambulance> maybeAmb = rm.acquireAmbulance(5, TimeUnit.SECONDS);
                if (maybeAmb.isEmpty()) {
                    System.out.println("[" + name + "] No hay ambulancias disponibles para " + em.getId() + ". Recolocar en cola.");
                    // reinsertar con leve retardo para evitar busy loop
                    em.setAssigned(false);
                    queue.offer(em, 2, TimeUnit.SECONDS);
                    continue;
                }

                Ambulance amb = maybeAmb.get();
                em.setAssigned(true);
                System.out.println("[" + name + "] Asignada ambulancia " + amb.getId() + " a emergencia " + em.getId());

                // simular viaje y atención en hilo separado (no bloquear dispatcher)
                new Thread(() -> handleEmergency(em, amb)).start();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(name + " interrumpido.");
        }
    }

    private void handleEmergency(Emergency em, Ambulance amb) {
        try {
            amb.setState(Resource.State.EN_ROUTE);
            // tiempo de desplazamiento proporcional a distancia (ej: 100ms * km)
            long travelMs = Math.max(300, (long) (em.getDistanceKm() * 100));
            Thread.sleep(travelMs);

            amb.setState(Resource.State.BUSY);
            // tiempo de atención depende de la severidad
            long attendMs = switch (em.getSeverity()) {
                case CRITICAL -> 4000L;
                case SERIOUS  -> 3000L;
                case MODERATE -> 2000L;
                default -> 1000L;
            };
            System.out.printf("Ambulancia %s atendiendo emergencia %d (dur=%dms)\n", amb.getId(), em.getId(), attendMs);
            Thread.sleep(attendMs);

            // finalizar atención
            System.out.printf("Ambulancia %s finalizó emergencia %d\n", amb.getId(), em.getId());
            rm.releaseAmbulance(amb);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.printf("Handler interrumpido para emergencia %d\n", em.getId());
            // en caso de interrupción, liberar recursos
            rm.releaseAmbulance(amb);
        }
    }

    public void shutdown() { running = false; }
}
