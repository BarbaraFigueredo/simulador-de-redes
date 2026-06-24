package com.netsim.application.usecase;

import com.netsim.application.SimulationStatistics;
import com.netsim.application.event.EventBus;
import com.netsim.domain.entity.*;
import com.netsim.domain.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SendPacketUseCase — Envio e Roteamento de Pacotes")
class SendPacketUseCaseTest {

    private SendPacketUseCase    useCase;
    private SimulationStatistics statistics;
    private TopologyService      topologyService;

    private Computer pc1;
    private Computer pc2;
    private Router   rt1;

    @BeforeEach
    void setUp() {
        EventBus.INSTANCE.clearAll();

        NetworkTopology topology = new NetworkTopology();
        DijkstraRoutingService routing = new DijkstraRoutingService();
        topologyService = new TopologyService(topology, routing);

        pc1 = new Computer("PC-01", "192.168.1.10");
        pc2 = new Computer("PC-02", "192.168.2.10");
        rt1 = new Router("RT-01", "10.0.0.1");

        topologyService.addDevice(pc1);
        topologyService.addDevice(pc2);
        topologyService.addDevice(rt1);

        topologyService.connect(pc1.getId(), rt1.getId(), 10, 100);
        topologyService.connect(rt1.getId(), pc2.getId(), 10, 100);

        SimulationService.SimulationConfig config = new SimulationService.SimulationConfig();
        SimulationService simulationService =
                new SimulationService(topologyService, routing, config);

        statistics = new SimulationStatistics();
        useCase    = new SendPacketUseCase(simulationService, statistics);
    }

    @Test
    @DisplayName("Deve entregar pacote com caminho válido")
    void shouldDeliverPacketOnValidPath() {
        List<NetworkDevice> capturedPath = new ArrayList<>();

        useCase.execute("192.168.1.10", "192.168.2.10", "Olá, rede!", 64, capturedPath::addAll);

        assertFalse(capturedPath.isEmpty(), "Caminho deve ser calculado");
        assertEquals(pc1, capturedPath.get(0), "Primeiro hop deve ser PC-01");
        assertEquals(pc2, capturedPath.get(capturedPath.size() - 1), "Último hop deve ser PC-02");
        assertEquals(1, statistics.getPacketsDelivered());
    }

    @Test
    @DisplayName("Deve contar pacote como perdido quando destino não existe")
    void shouldDropPacketToUnknownDestination() {
        useCase.execute("192.168.1.10", "99.99.99.99", "teste", 64, path -> {});

        assertEquals(0, statistics.getPacketsDelivered());
        assertEquals(1, statistics.getPacketsDropped());
    }

    @Test
    @DisplayName("Deve incrementar contador de enviados sempre")
    void shouldIncrementSentCounter() {
        useCase.execute("192.168.1.10", "192.168.2.10", "msg", 64, path -> {});
        useCase.execute("192.168.1.10", "99.99.99.99", "msg2", 64, path -> {});

        assertEquals(2, statistics.getPacketsSent());
    }

    @Test
    @DisplayName("Pacote com TTL muito pequeno não deve chegar ao destino em caminho longo")
    void shouldExpireWithLowTtl() {
        // TTL=1: apenas 1 decremento permitido, mas há 1 roteador no caminho
        // Então o TTL expira no roteador antes de chegar
        useCase.execute("192.168.1.10", "192.168.2.10", "msg", 1, path -> {});

        assertEquals(0, statistics.getPacketsDelivered(), "Pacote com TTL=1 deve expirar no roteador");
        assertEquals(1, statistics.getPacketsDropped());
    }
}