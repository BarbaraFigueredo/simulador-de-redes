package com.netsim.application.usecase;

import com.netsim.application.event.EventBus;
import com.netsim.application.event.SimulationEventType;
import com.netsim.domain.entity.*;
import com.netsim.domain.service.TopologyService;
import com.netsim.domain.valueobject.DeviceType;

/**
 * Caso de uso: adicionar um dispositivo à topologia.
 *
 * Responsabilidades:
 *  1. Criar a entidade correta (Factory Method Pattern)
 *  2. Validar unicidade do IP
 *  3. Delegar ao TopologyService
 *  4. Publicar evento no EventBus
 */
public class AddDeviceUseCase {

    private final TopologyService topologyService;

    public AddDeviceUseCase(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    public NetworkDevice execute(String name, String ipAddress, DeviceType type) {
        return execute(name, ipAddress, type, null);
    }

    public NetworkDevice execute(String name, String ipAddress, DeviceType type, String extra) {
        validateIpUniqueness(ipAddress);

        NetworkDevice device = createDevice(type, name, ipAddress, extra);
        topologyService.addDevice(device);

        EventBus.INSTANCE.publish(SimulationEventType.DEVICE_ADDED,
                String.format("Dispositivo adicionado: %s (%s)", name, ipAddress),
                device);

        return device;
    }

    private void validateIpUniqueness(String ip) {
        topologyService.findByIp(ip).ifPresent(existing -> {
            throw new IllegalArgumentException("IP já em uso: " + ip + " por " + existing.getName());
        });
    }

    /** Factory Method: cria o tipo correto de dispositivo. */
    private NetworkDevice createDevice(DeviceType type, String name, String ip, String extra) {
        return switch (type) {
            case COMPUTER -> new Computer(name, ip, extra != null ? extra : "Linux");
            case ROUTER   -> new Router(name, ip);
            case SWITCH   -> new Switch(name, ip);
        };
    }
}