package com.iudigital.emergencias.medicas;

import java.util.concurrent.atomic.AtomicReference;

public class Ambulance implements Resource {

    private final String id;  // Identificador de la ambulancia
    private final int capacity; // Número de pacientes que puede transportar
    private final AtomicReference<State> state; // Estado de la ambulancia

    public Ambulance(String id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        this.state = new AtomicReference<>(State.AVAILABLE); // Estado inicial: disponible
    }

    // Métodos de la interfaz Resource

    @Override
    public String getId() {
        return id;
    }

    @Override
    public State getState() {
        return state.get();
    }

    @Override
    public void setState(State state) {
        this.state.set(state);
    }

    // Getter adicional opcional para capacidad
    public int getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return "Ambulance{ " +
                "id='" + id + '\'' +
                ", capacity=" + capacity +
                ", state=" + state.get() +
                " }";
    }
}
