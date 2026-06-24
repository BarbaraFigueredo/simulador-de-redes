package com.netsim.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netsim.application.dto.ConnectionDTO;
import com.netsim.application.dto.DeviceDTO;
import com.netsim.application.dto.TopologyDTO;
import com.netsim.application.event.EventBus;
import com.netsim.application.event.SimulationEventType;
import com.netsim.domain.entity.*;
import com.netsim.domain.service.DijkstraRoutingService;
import com.netsim.domain.service.TopologyService;
import com.netsim.domain.valueobject.DeviceType;

import java.io.File;
import java.io.IOException;

public class ImportTopologyUseCase {

    private final NetworkTopology        topology;
    private final DijkstraRoutingService routingService;
    private final ObjectMapper           mapper;

    public ImportTopologyUseCase(NetworkTopology topology,
                                 DijkstraRoutingService routingService) {
        this.topology       = topology;
        this.routingService = routingService;
        this.mapper         = new ObjectMapper();
    }

    public TopologyService execute(File file) throws IOException {
        TopologyDTO dto = mapper.readValue(file, TopologyDTO.class);

        topology.clear();
        TopologyService service = new TopologyService(topology, routingService);

        for (DeviceDTO d : dto.devices) {
            NetworkDevice device = createDevice(d);
            device.setPositionX(d.positionX);
            device.setPositionY(d.positionY);
            topology.addDevice(device);
        }

        for (ConnectionDTO c : dto.connections) {
            topology.findDeviceById(c.deviceAId).ifPresent(a ->
                topology.findDeviceById(c.deviceBId).ifPresent(b ->
                    topology.addConnection(a, b, c.latencyMs, c.bandwidthMbps)));
        }

        routingService.updateAllRoutingTables(topology);

        EventBus.INSTANCE.publish(SimulationEventType.TOPOLOGY_LOADED,
                "Topologia carregada: " + dto.devices.size() + " dispositivos, "
                + dto.connections.size() + " conexões");

        return service;
    }

    private NetworkDevice createDevice(DeviceDTO d) {
        DeviceType type = DeviceType.valueOf(d.type);
        return switch (type) {
            case COMPUTER -> {
                Computer c = new Computer(d.name, d.ipAddress,
                        d.operatingSystem != null ? d.operatingSystem : "Linux");
                yield c;
            }
            case ROUTER  -> new Router(d.name, d.ipAddress);
            case SWITCH  -> new Switch(d.name, d.ipAddress);
        };
    }
}