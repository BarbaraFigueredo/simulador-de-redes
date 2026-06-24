package com.netsim.application.event;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Barramento de eventos — Observer Pattern.
 *
 * Desacopla produtores (use cases) de consumidores (controllers da UI).
 * A UI registra listeners para tipos específicos de evento.
 * Os use cases publicam eventos sem conhecer quem os consome.
 *
 * Implementado como Singleton via enum para thread-safety garantida pela JVM.
 */
public enum EventBus {
    INSTANCE;

    private final Map<SimulationEventType, List<Consumer<SimulationEvent>>> listeners =
            new EnumMap<>(SimulationEventType.class);

    public void subscribe(SimulationEventType type, Consumer<SimulationEvent> listener) {
        listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
    }

    public void unsubscribeAll(SimulationEventType type) {
        listeners.remove(type);
    }

    public void clearAll() {
        listeners.clear();
    }

    public void publish(SimulationEvent event) {
        List<Consumer<SimulationEvent>> eventListeners = listeners.get(event.getType());
        if (eventListeners != null) {
            for (Consumer<SimulationEvent> listener : eventListeners) {
                listener.accept(event);
            }
        }
    }

    public void publish(SimulationEventType type, String message) {
        publish(new SimulationEvent(type, message));
    }

    public void publish(SimulationEventType type, String message, Object payload) {
        publish(new SimulationEvent(type, message, payload));
    }
}