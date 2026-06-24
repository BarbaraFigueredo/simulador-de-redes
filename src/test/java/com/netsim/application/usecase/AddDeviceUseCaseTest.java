package com.netsim.application.usecase;

import com.netsim.application.event.EventBus;
import com.netsim.application.event.SimulationEventType;
import com.netsim.domain.entity.NetworkDevice;
import com.netsim.domain.entity.NetworkTopology;
import com.netsim.domain.service.DijkstraRoutingService;
import com.netsim.domain.service.TopologyService;
import com.netsim.domain.valueobject.DeviceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AddDeviceUseCase — Adição de Dispositivos")
class AddDeviceUseCaseTest {

    private AddDeviceUseCase useCase;
    private TopologyService  topologyService;

    @BeforeEach
    void setUp() {
        EventBus.INSTANCE.clearAll();
        NetworkTopology topology = new NetworkTopology();
        topologyService = new TopologyService(topology, new DijkstraRoutingService());
        useCase = new AddDeviceUseCase(topologyService);
    }

    @Test
    @DisplayName("Deve adicionar um computador com sucesso")
    void shouldAddComputer() {
        NetworkDevice device = useCase.execute("PC-01", "192.168.1.1", DeviceType.COMPUTER);

        assertNotNull(device.getId());
        assertEquals("PC-01", device.getName());
        assertEquals("192.168.1.1", device.getIpAddress());
        assertEquals(DeviceType.COMPUTER, device.getType());
    }

    @Test
    @DisplayName("Deve adicionar roteador com sucesso")
    void shouldAddRouter() {
        NetworkDevice router = useCase.execute("RT-01", "10.0.0.1", DeviceType.ROUTER);

        assertEquals(DeviceType.ROUTER, router.getType());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar usar IP duplicado")
    void shouldThrowOnDuplicateIp() {
        useCase.execute("PC-01", "192.168.1.1", DeviceType.COMPUTER);

        assertThrows(IllegalArgumentException.class, () ->
                useCase.execute("PC-02", "192.168.1.1", DeviceType.COMPUTER),
                "Deve rejeitar IP já cadastrado");
    }

    @Test
    @DisplayName("Deve publicar evento DEVICE_ADDED ao adicionar dispositivo")
    void shouldPublishEvent() {
        AtomicBoolean eventReceived = new AtomicBoolean(false);
        EventBus.INSTANCE.subscribe(SimulationEventType.DEVICE_ADDED,
                e -> eventReceived.set(true));

        useCase.execute("PC-01", "192.168.1.1", DeviceType.COMPUTER);

        assertTrue(eventReceived.get(), "Evento DEVICE_ADDED deve ser publicado");
    }

    @Test
    @DisplayName("Dispositivo deve ser encontrado na topologia após adição")
    void shouldBeRetrievableFromTopology() {
        useCase.execute("SW-01", "192.168.1.254", DeviceType.SWITCH);

        assertTrue(topologyService.findByIp("192.168.1.254").isPresent());
    }
}