package com.iudigital.emergencias.medicas;

import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Iniciando Sistema de Emergencias - com.iudigital");

        // Cola priorizada (se basa en compareTo de Emergency)
        BlockingQueue<Emergency> queue = new PriorityBlockingQueue<>();

        // Inicializar recursos (ejemplo: 4 ambulancias)
        ResourceManager rm = ResourceManager.getInstance();
        List<Ambulance> ambulances = List.of(
                new Ambulance("AMB-1", 2),
                new Ambulance("AMB-2", 2),
                new Ambulance("AMB-3", 1),
                new Ambulance("AMB-4", 1)
        );
        rm.initAmbulances(ambulances);

        // Operadores (productores): 2 hilos que generan llamadas
        Operator op1 = new Operator("Operator-1", queue);
        Operator op2 = new Operator("Operator-2", queue);
        Thread opThread1 = new Thread(op1);
        Thread opThread2 = new Thread(op2);

        // Despachadores: 2 hilos consumidores
        Dispatcher disp1 = new Dispatcher("Dispatcher-1", queue);
        Dispatcher disp2 = new Dispatcher("Dispatcher-2", queue);
        Thread dThread1 = new Thread(disp1);
        Thread dThread2 = new Thread(disp2);

        // Monitor
        Monitor monitor = new Monitor(queue);
        monitor.start();

        // Iniciar hilos
        opThread1.start();
        opThread2.start();
        dThread1.start();
        dThread2.start();

        // Ejecutar simulación por tiempo controlado (ej: 60 segundos)
        TimeUnit.SECONDS.sleep(60);

        // Shutdown ordenado
        System.out.println("Deteniendo operadores y despachadores...");
        op1.shutdown();
        op2.shutdown();
        disp1.shutdown();
        disp2.shutdown();

        opThread1.interrupt();
        opThread2.interrupt();
        dThread1.interrupt();
        dThread2.interrupt();

        monitor.stop();

        System.out.println("Simulación finalizada.");
    }
}

