package com.iudigital.emergencias.medicas;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ResourceManager {
    private static final ResourceManager INSTANCE = new ResourceManager();

    private final ConcurrentMap<String, Ambulance> ambulances = new ConcurrentHashMap<>();
    private final Semaphore ambulanceSemaphore; // controla disponibilidad
    private final ReentrantLock lock = new ReentrantLock(true);

    private ResourceManager() {
        // inicialización por defecto (puede cambiarse con init)
        ambulanceSemaphore = new Semaphore(0);
    }

    public static ResourceManager getInstance() {
        return INSTANCE;
    }

    // inicializar recursos (llamar al inicio)
    public void initAmbulances(List<Ambulance> list) {
        lock.lock();
        try {
            ambulances.clear();
            list.forEach(a -> ambulances.put(a.getId(), a));
            // re-create semaphore con nuevo número disponible = cantidad inicial
            int permits = list.size();
            // reflectively replace? Simpler: drain and release
            while (ambulanceSemaphore.availablePermits() > 0) ambulanceSemaphore.tryAcquire();
            ambulanceSemaphore.release(permits);
        } finally {
            lock.unlock();
        }
    }

    public Optional<Ambulance> acquireAmbulance(long timeout, TimeUnit unit) throws InterruptedException {
        boolean ok = ambulanceSemaphore.tryAcquire(timeout, unit);
        if (!ok) return Optional.empty();
        // buscar una ambulancia disponible de forma atómica
        for (Ambulance a : ambulances.values()) {
            if (a.getState() == Resource.State.AVAILABLE) {
                if (a.getState() == Resource.State.AVAILABLE) {
                    a.setState(Resource.State.EN_ROUTE);
                    return Optional.of(a);
                }
            }
        }
        // si no la encontramos, liberar permiso y retornar vacío
        ambulanceSemaphore.release();
        return Optional.empty();
    }

    public void releaseAmbulance(Ambulance a) {
        a.setState(Resource.State.AVAILABLE);
        ambulanceSemaphore.release();
    }

    public List<Ambulance> snapshotAmbulances() {
        return ambulances.values().stream().collect(Collectors.toList());
    }

    // marcar ambulancia temporalmente no disponible (por fallo)
    public void setAmbulanceUnavailable(String id) {
        Ambulance a = ambulances.get(id);
        if (a != null) {
            a.setState(Resource.State.UNAVAILABLE);
            // reduce permits if needed
            // try to acquire one permit if available
            ambulanceSemaphore.tryAcquire();
        }
    }

    // obtener resumen simple
    public String status() {
        long available = ambulances.values().stream().filter(x -> x.getState() == Resource.State.AVAILABLE).count();
        long enroute = ambulances.values().stream().filter(x -> x.getState() == Resource.State.EN_ROUTE).count();
        long busy = ambulances.values().stream().filter(x -> x.getState() == Resource.State.BUSY).count();
        long down = ambulances.values().stream().filter(x -> x.getState() == Resource.State.UNAVAILABLE).count();
        return String.format("Ambulances - available: %d, enroute: %d, busy: %d, down: %d",
                available, enroute, busy, down);
    }
}

