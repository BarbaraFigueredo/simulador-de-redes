package com.netsim.application.usecase;

import com.netsim.application.event.EventBus;
import com.netsim.application.event.SimulationEventType;
import com.netsim.domain.entity.Connection;
import com.netsim.domain.service.TopologyService;

public class ConnectDevicesUseCase {

    private final TopologyService topologyService;

    public ConnectDevicesUseCase(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    public Connection execute(String deviceIdA, String deviceIdB, int latencyMs, int bandwidthMbps) {
        Connection conn = topologyService.connect(deviceIdA, deviceIdB, latencyMs, bandwidthMbps);

        EventBus.INSTANCE.publish(SimulationEventType.CONNECTION_ADDED,
                String.format("Conexão criada: %s ↔ %s | %dms | %dMbps",
                        conn.getDeviceA().getName(), conn.getDeviceB().getName(),
                        latencyMs, bandwidthMbps),
                conn);
        return conn;
    }
}