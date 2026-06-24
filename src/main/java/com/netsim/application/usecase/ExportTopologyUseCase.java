package com.netsim.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.netsim.application.dto.ConnectionDTO;
import com.netsim.application.dto.DeviceDTO;
import com.netsim.application.dto.TopologyDTO;
import com.netsim.domain.entity.Computer;
import com.netsim.domain.entity.Connection;
import com.netsim.domain.entity.NetworkDevice;
import com.netsim.domain.entity.NetworkTopology;

import java.io.File;
import java.io.IOException;

public class ExportTopologyUseCase {

    private final NetworkTopology topology;
    private final ObjectMapper    mapper;

    public ExportTopologyUseCase(NetworkTopology topology) {
        this.topology = topology;
        this.mapper   = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void execute(File file) throws IOException {
        TopologyDTO dto = new TopologyDTO();

        for (NetworkDevice device : topology.getAllDevices()) {
            String os = (device instanceof Computer c) ? c.getOperatingSystem() : null;
            dto.devices.add(new DeviceDTO(
                    device.getId(), device.getName(), device.getIpAddress(),
                    device.getType().name(), device.getStatus().name(),
                    os, device.getPositionX(), device.getPositionY()));
        }

        for (Connection conn : topology.getAllConnections()) {
            dto.connections.add(new ConnectionDTO(
                    conn.getId(), conn.getDeviceA().getId(), conn.getDeviceB().getId(),
                    conn.getLatencyMs(), conn.getBandwidthMbps()));
        }

        mapper.writeValue(file, dto);
    }
}