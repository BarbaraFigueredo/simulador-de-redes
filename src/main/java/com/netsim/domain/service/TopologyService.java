package com.netsim.domain.service;

import com.netsim.domain.entity.*;

import java.util.Collection;
import java.util.Optional;

/**
 * Serviço de domínio que encapsula operações na topologia.
 *
 * Princípio SRP: responsabilidade única de gerenciar o estado
 * da topologia e disparar atualizações de roteamento.
 *
 * Princípio DIP: depende da abstração RoutingStrategy,
 * não de uma implementação concreta.
 */
public class TopologyService {

    private final NetworkTopology      topology;
    private final DijkstraRoutingService routingService;

    public TopologyService(NetworkTopology topology, DijkstraRoutingService routingService) {
        this.topology       = topology;
        this.routingService = routingService;
    }

    public void addDevice(NetworkDevice device) {
        topology.addDevice(device);
        routingService.updateAllRoutingTables(topology);
    }

    public void removeDevice(String deviceId) {
        topology.removeDevice(deviceId);
        routingService.updateAllRoutingTables(topology);
    }

    public Connection connect(String deviceIdA, String deviceIdB, int latencyMs, int bandwidthMbps) {
        NetworkDevice a = topology.findDeviceById(deviceIdA)
                .orElseThrow(() -> new IllegalArgumentException("Dispositivo não encontrado: " + deviceIdA));
        NetworkDevice b = topology.findDeviceById(deviceIdB)
                .orElseThrow(() -> new IllegalArgumentException("Dispositivo não encontrado: " + deviceIdB));

        Connection conn = topology.addConnection(a, b, latencyMs, bandwidthMbps);
        routingService.updateAllRoutingTables(topology);
        return conn;
    }

    public void disconnect(String connectionId) {
        topology.removeConnection(connectionId);
        routingService.updateAllRoutingTables(topology);
    }

    public Optional<NetworkDevice> findByIp(String ip) {
        return topology.findDeviceByIp(ip);
    }

    public Optional<NetworkDevice> findById(String id) {
        return topology.findDeviceById(id);
    }

    public Collection<NetworkDevice> getAllDevices() {
        return topology.getAllDevices();
    }

    public NetworkTopology getTopology() {
        return topology;
    }
}