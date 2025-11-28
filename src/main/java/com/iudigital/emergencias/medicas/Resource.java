package com.iudigital.emergencias.medicas;

public interface Resource {

    // Estados posibles de un recurso
    enum State {
        AVAILABLE,      // Disponible
        EN_ROUTE,       // En camino
        BUSY,           // Ocupado / Atendiendo
        UNAVAILABLE     // Fuera de servicio
    }

    // Métodos que toda clase de recurso debe tener
    String getId();

    State getState();

    void setState(State state);
}

