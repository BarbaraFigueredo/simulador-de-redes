package com.netsim.domain.entity;

import java.util.Objects;
import java.util.UUID;

/**
 * Representa um enlace (link) físico entre dois dispositivos de rede.
 *
 * Conceito de redes: enlaces são os meios físicos de transmissão
 * (cabos Ethernet, fibra óptica, Wi-Fi). Cada enlace possui:
 *  - Latência: tempo de propagação do sinal (ms)
 *  - Largura de banda: capacidade máxima (Mbps)
 *
 * No algoritmo de Dijkstra, a latência é usada como peso da aresta.
 * O caminho de menor custo minimiza a latência total.
 */
public class Connection {

    private final String id;
    private final NetworkDevice deviceA;
    private final NetworkDevice deviceB;
    private int latencyMs;
    private int bandwidthMbps;

    public Connection(NetworkDevice deviceA, NetworkDevice deviceB) {
        this(deviceA, deviceB, 10, 100);
    }

    public Connection(NetworkDevice deviceA, NetworkDevice deviceB, int latencyMs, int bandwidthMbps) {
        this.id            = UUID.randomUUID().toString();
        this.deviceA       = Objects.requireNonNull(deviceA);
        this.deviceB       = Objects.requireNonNull(deviceB);
        this.latencyMs     = latencyMs;
        this.bandwidthMbps = bandwidthMbps;
    }

    /** Verifica se este enlace conecta os dois dispositivos informados. */
    public boolean connects(NetworkDevice d1, NetworkDevice d2) {
        return (deviceA.equals(d1) && deviceB.equals(d2))
                || (deviceA.equals(d2) && deviceB.equals(d1));
    }

    /** Dado um dispositivo, retorna o outro lado do enlace. */
    public NetworkDevice getOtherEnd(NetworkDevice device) {
        if (device.equals(deviceA)) return deviceB;
        if (device.equals(deviceB)) return deviceA;
        throw new IllegalArgumentException("Dispositivo não pertence a este enlace: " + device.getName());
    }

    public String getId()           { return id; }
    public NetworkDevice getDeviceA()      { return deviceA; }
    public NetworkDevice getDeviceB()      { return deviceB; }
    public int getLatencyMs()              { return latencyMs; }
    public int getBandwidthMbps()          { return bandwidthMbps; }
    public void setLatencyMs(int ms)       { this.latencyMs = ms; }
    public void setBandwidthMbps(int mbps) { this.bandwidthMbps = mbps; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Connection)) return false;
        return id.equals(((Connection) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format("Connection[%s ↔ %s | %dms | %dMbps]",
                deviceA.getName(), deviceB.getName(), latencyMs, bandwidthMbps);
    }
}