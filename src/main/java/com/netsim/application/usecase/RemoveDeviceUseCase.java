package com.netsim.application.usecase;

import com.netsim.application.event.EventBus;
import com.netsim.application.event.SimulationEventType;
import com.netsim.domain.entity.NetworkDevice;
import com.netsim.domain.service.TopologyService;

public class RemoveDeviceUseCase {

    private final TopologyService topologyService;

    public RemoveDeviceUseCase(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    public void execute(String deviceId) {
        NetworkDevice device = topologyService.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Dispositivo não encontrado: " + deviceId));

        topologyService.removeDevice(deviceId);

        EventBus.INSTANCE.publish(SimulationEventType.DEVICE_REMOVED,
                String.format("Dispositivo removido: %s", device.getName()),
                deviceId);
    }
}