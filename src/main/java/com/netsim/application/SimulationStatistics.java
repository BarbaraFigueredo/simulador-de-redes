package com.netsim.application;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Estatísticas em tempo real da simulação.
 * Usa AtomicInteger para segurança em acessos concorrentes.
 */
public class SimulationStatistics {

    private final AtomicInteger packetsSent      = new AtomicInteger(0);
    private final AtomicInteger packetsDelivered = new AtomicInteger(0);
    private final AtomicInteger packetsDropped   = new AtomicInteger(0);
    private final AtomicLong    totalLatencyMs   = new AtomicLong(0);

    public void recordSent()                 { packetsSent.incrementAndGet(); }
    public void recordDelivered(int latency) {
        packetsDelivered.incrementAndGet();
        totalLatencyMs.addAndGet(latency);
    }
    public void recordDropped()              { packetsDropped.incrementAndGet(); }

    public int getPacketsSent()      { return packetsSent.get(); }
    public int getPacketsDelivered() { return packetsDelivered.get(); }
    public int getPacketsDropped()   { return packetsDropped.get(); }

    public double getAverageLatencyMs() {
        int delivered = packetsDelivered.get();
        return delivered == 0 ? 0.0 : (double) totalLatencyMs.get() / delivered;
    }

    public double getDeliveryRate() {
        int sent = packetsSent.get();
        return sent == 0 ? 0.0 : (double) packetsDelivered.get() / sent * 100.0;
    }

    public void reset() {
        packetsSent.set(0);
        packetsDelivered.set(0);
        packetsDropped.set(0);
        totalLatencyMs.set(0);
    }

    @Override
    public String toString() {
        return String.format(
                "Enviados: %d | Entregues: %d | Perdidos: %d | Taxa: %.1f%% | Latência média: %.1fms",
                getPacketsSent(), getPacketsDelivered(), getPacketsDropped(),
                getDeliveryRate(), getAverageLatencyMs());
    }
}