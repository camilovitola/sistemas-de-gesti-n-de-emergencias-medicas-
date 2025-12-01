# Sistema de Gestión de Emergencias Médicas (Concurrencia en Java)
**Proyecto académico – Programación Concurrente – NetBeans 21 / Java 17 / Maven**  
**Autor:** camilo vitola   
**Grupo:** com.iudigital  
**Repositorio:** https://github.com/camilovitola/sistemas-de-gesti-n-de-emergencias-medicas-

---

## 📌 Descripción del Sistema
Este proyecto implementa un **simulador concurrente** que gestiona emergencias médicas en tiempo real.  
El sistema modela:

- Operadores que reciben llamadas de emergencia
- Una cola priorizada (PriorityBlockingQueue)
- Despachadores que asignan ambulancias
- Recursos compartidos (ambulancias)
- Un monitor que muestra el estado del sistema cada 2 segundos

El objetivo principal es demostrar el uso de **multithreading**, **sincronización**, **patrones concurrentes** y **gestión de recursos** en Java.

---

## 🚑 Flujo General del Sistema


---

## 🧩 Arquitectura del Proyecto

### **Clases principales**
| Clase | Descripción |
|-------|-------------|
| `Emergency` | Modelo que representa un evento médico con severidad, ubicación y timestamp. Implementa `Comparable` para priorización. |
| `Operator` | Hilo productor que genera emergencias aleatorias. |
| `Dispatcher` | Hilo consumidor que toma emergencias de la cola y asigna recursos. |
| `ResourceManager` | **Singleton** que administra ambulancias usando `Semaphore` y `ConcurrentHashMap`. |
| `Ambulance` | Recurso compartido gestionado de forma segura con `AtomicReference<State>`. |
| `Monitor` | Usa `ScheduledExecutorService` para monitorear sistema en tiempo real. |
| `Main` | Punto de entrada del sistema. Configura hilos, recursos y arranca el simulador. |

---

## 🕸️ Patrones y Técnicas de Concurrencia Utilizadas

### ✔ **Producer – Consumer**
- Operadores producen emergencias
- Despachadores consumen la cola
- Implementado con `PriorityBlockingQueue`

### ✔ **Singleton**
- `ResourceManager` es único en todo el sistema

### ✔ **Thread-safe collections**
- `ConcurrentHashMap` para recursos
- `PriorityBlockingQueue` para emergencias

### ✔ **Semáforos (Semaphore)**
- Controlan la disponibilidad de ambulancias
- Sincronizan acceso seguro al recurso compartido

### ✔ **Locks**
- `ReentrantLock` previene condiciones de carrera en selección de ambulancias

### ✔ **Atomic Variables**
- `AtomicReference<State>` para estados de ambulancias

### ✔ **Scheduled Tasks**
- Monitor en tiempo real con `ScheduledExecutorService`

---

## ⚠️ Estrategias de Prioridad de Emergencias

La prioridad se calcula usando:

1. **Severidad** (CRITICAL > SERIOUS > MODERATE > MINOR)  
2. **Tiempo de espera** → emergencias viejas suben prioridad (evita starvation)  
3. **Distancia** → ambulancias más cercanas son asignadas primero  

Todo gestionado en `Emergency.compareTo()`.

---

## 🧵 Problemas de Concurrencia Solucionados

### 1. **Race condition** al asignar ambulancias  
→ Solución: `ReentrantLock` + operaciones atómicas

### 2. **Deadlocks**  
→ Solución: nunca se anidan locks + `tryAcquire(timeout)`

### 3. **Starvation**  
→ Solución: agregar `reportedAt` a la comparación de prioridades

### 4. **Busy waiting**  
→ Solución: reencolar emergencias con `offer(timeout)`

### 5. **Inconsistencia entre Semaphore y estados**  
→ Solución: `tryAcquire` + setState atómico + `release` seguro

---

## ▶️ Cómo Ejecutar el Proyecto

### **Desde NetBeans (más fácil):**
1. Abrir NetBeans 21
2. Abrir el proyecto `sistemas-de-gesti-n-de-emergencias-medicas-`
3. Seleccionar `Main.java` como clase principal
4. Presionar **Run** (F6)

### Resultado esperado:
- Generación de emergencias
- Asignación de ambulancias
- Monitoreo periódico
- Simulación fluida con logs detallados

---

## ▶️ Ejecución desde la terminal (Maven)

### Compilar:
```bash
mvn clean package


