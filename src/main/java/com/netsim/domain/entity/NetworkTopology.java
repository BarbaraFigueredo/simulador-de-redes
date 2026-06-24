package com.netsim.domain.entity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Grafo da topologia de rede.
 *
 * Conceito de redes: a topologia descreve como os dispositivos estão
 * interconectados. Os principais tipos são:
 *  - Bus, Star, Ring, Mesh, Tree, Hybrid
 *
 * Implementada como um grafo não-dirigido com:
 *  - Vértices: NetworkDevice
 *  - Arestas:  Connection (com peso = latência)
 *
 * Segue o padrão Aggregate Root do DDD: é a única porta de entrada
 * para modificar dispositivos e conexões da rede.
 */
public class NetworkTopology {

    private final Map<String, NetworkDevice> devices     = new LinkedHashMap<>();
    private final List<Connection>           connections = new ArrayList<>();

    // --- Gestão de dispositivos ---

    public void addDevice(NetworkDevice device) {
        Objects.requireNonNull(device, "Dispositivo não pode ser nulo");
        if (devices.containsKey(device.getId())) {
            throw new IllegalArgumentException("Dispositivo já existe: " + device.getName());
        }
        devices.put(device.getId(), device);
    }

    public void removeDevice(String deviceId) {
        NetworkDevice device = devices.remove(deviceId);
        if (device != null) {
            connections.removeIf(c ->
                    c.getDeviceA().getId().equals(deviceId)
                    || c.getDeviceB().getId().equals(deviceId));
        }
    }

    public Optional<NetworkDevice> findDeviceById(String id) {
        return Optional.ofNullable(devices.get(id));
    }

    public Optional<NetworkDevice> findDeviceByIp(String ip) {
        return devices.values().stream()
                .filter(d -> d.getIpAddress().equals(ip))
                .findFirst();
    }

    public Optional<NetworkDevice> findDeviceByName(String name) {
        return devices.values().stream()
                .filter(d -> d.getName().equals(name))
                .findFirst();
    }

    public Collection<NetworkDevice> getAllDevices() {
        return Collections.unmodifiableCollection(devices.values());
    }

    // --- Gestão de conexões ---

    public Connection addConnection(NetworkDevice a, NetworkDevice b) {
        return addConnection(a, b, 10, 100);
    }

    public Connection addConnection(NetworkDevice a, NetworkDevice b, int latencyMs, int bandwidthMbps) {
        if (!devices.containsKey(a.getId()) || !devices.containsKey(b.getId())) {
            throw new IllegalArgumentException("Ambos os dispositivos devem estar na topologia.");
        }
        if (alreadyConnected(a, b)) {
            throw new IllegalStateException("Dispositivos já estão conectados.");
        }
        Connection conn = new Connection(a, b, latencyMs, bandwidthMbps);
        connections.add(conn);
        return conn;
    }

    public void removeConnection(String connectionId) {
        connections.removeIf(c -> c.getId().equals(connectionId));
    }

    public boolean alreadyConnected(NetworkDevice a, NetworkDevice b) {
        return connections.stream().anyMatch(c -> c.connects(a, b));
    }

    /** Retorna todos os vizinhos diretamente conectados a um dispositivo. */
    public List<NetworkDevice> getNeighbors(NetworkDevice device) {
        return connections.stream()
                .filter(c -> c.getDeviceA().equals(device) || c.getDeviceB().equals(device))
                .map(c -> c.getOtherEnd(device))
                .collect(Collectors.toList());
    }

    /** Retorna o enlace entre dois dispositivos, se existir. */
    public Optional<Connection> getConnection(NetworkDevice a, NetworkDevice b) {
        return connections.stream()
                .filter(c -> c.connects(a, b))
                .findFirst();
    }

    public List<Connection> getAllConnections() {
        return Collections.unmodifiableList(connections);
    }

    public int getDeviceCount()     { return devices.size(); }
    public int getConnectionCount() { return connections.size(); }

    public void clear() {
        devices.clear();
        connections.clear();
    }
}