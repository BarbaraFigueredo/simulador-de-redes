package com.netsim.application.event;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Evento imutável gerado durante a simulação.
 * Transporta o tipo, mensagem e payload opcional.
 */
public class SimulationEvent {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final SimulationEventType type;
    private final String              message;
    private final Object              payload;
    private final String              timestamp;

    public SimulationEvent(SimulationEventType type, String message) {
        this(type, message, null);
    }

    public SimulationEvent(SimulationEventType type, String message, Object payload) {
        this.type      = type;
        this.message   = message;
        this.payload   = payload;
        this.timestamp = LocalTime.now().format(FMT);
    }

    public SimulationEventType getType()    { return type; }
    public String getMessage()             { return message; }
    public Object getPayload()             { return payload; }
    public String getTimestamp()           { return timestamp; }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + message;
    }
}