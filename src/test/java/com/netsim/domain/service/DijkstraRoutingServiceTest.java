package com.netsim.domain.service;

import com.netsim.domain.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes do algoritmo de Dijkstra.
 *
 * Topologia de teste:
 *
 *   PC1 --10ms-- RT1 --5ms-- RT2 --10ms-- PC2
 *                 |                        |
 *                 +--------20ms------------+
 *
 * Menor caminho: PC1 → RT1 → RT2 → PC2  (custo = 25ms)
 * Caminho alternativo: PC1 → RT1 → PC2  (custo = 30ms via ligação direta)
 *
 * O Dijkstra deve sempre escolher o de menor custo.
 */
@DisplayName("DijkstraRoutingService — Algoritmo de Menor Caminho")
class DijkstraRoutingServiceTest {

    private DijkstraRoutingService service;
    private NetworkTopology        topology;

    private Computer pc1;
    private Computer pc2;
    private Router   rt1;
    private Router   rt2;

    @BeforeEach
    void setUp() {
        service  = new DijkstraRoutingService();
        topology = new NetworkTopology();

        pc1 = new Computer("PC-01", "192.168.1.10");
        pc2 = new Computer("PC-02", "192.168.2.10");
        rt1 = new Router("RT-01", "10.0.0.1");
        rt2 = new Router("RT-02", "10.0.0.2");

        topology.addDevice(pc1);
        topology.addDevice(pc2);
        topology.addDevice(rt1);
        topology.addDevice(rt2);
    }

    @Test
    @DisplayName("Deve encontrar caminho direto entre dois nós conectados")
    void shouldFindDirectPath() {
        topology.addConnection(pc1, rt1, 10, 100);
        topology.addConnection(rt1, pc2, 20, 100);

        List<NetworkDevice> path = service.findPath(topology, pc1, pc2);

        assertAll(
                () -> assertFalse(path.isEmpty(), "Caminho não deve ser vazio"),
                () -> assertEquals(3, path.size(), "Caminho deve ter 3 nós"),
                () -> assertEquals(pc1, path.get(0), "Origem deve ser PC-01"),
                () -> assertEquals(rt1, path.get(1), "Meio deve ser RT-01"),
                () -> assertEquals(pc2, path.get(2), "Destino deve ser PC-02")
        );
    }

    @Test
    @DisplayName("Deve escolher o caminho de menor custo entre dois caminhos alternativos")
    void shouldPickCheaperPath() {
        // Caminho 1: PC1 → RT1 → RT2 → PC2 (custo: 10+5+10 = 25ms)
        topology.addConnection(pc1, rt1, 10, 100);
        topology.addConnection(rt1, rt2, 5, 100);
        topology.addConnection(rt2, pc2, 10, 100);
        // Caminho 2: PC1 → RT1 → PC2 diretamente (custo: 10+20 = 30ms)
        topology.addConnection(rt1, pc2, 20, 100);

        List<NetworkDevice> path = service.findPath(topology, pc1, pc2);

        assertEquals(4, path.size(), "O caminho mais curto tem 4 nós (via RT1 → RT2)");
        assertTrue(path.contains(rt2), "O caminho ótimo deve passar pelo RT-02");
    }

    @Test
    @DisplayName("Deve retornar lista vazia se não houver caminho")
    void shouldReturnEmptyWhenNoPath() {
        // PC1 e PC2 estão na topologia mas sem conexão entre eles
        List<NetworkDevice> path = service.findPath(topology, pc1, pc2);

        assertTrue(path.isEmpty(), "Deve retornar caminho vazio quando não há rota");
    }

    @Test
    @DisplayName("Deve retornar lista com apenas a origem quando origem == destino")
    void shouldReturnSourceWhenSameNode() {
        List<NetworkDevice> path = service.findPath(topology, pc1, pc1);

        assertEquals(1, path.size());
        assertEquals(pc1, path.get(0));
    }

    @Test
    @DisplayName("Deve ignorar dispositivos inativos ao calcular o caminho")
    void shouldIgnoreInactiveDevices() {
        topology.addConnection(pc1, rt1, 10, 100);
        topology.addConnection(rt1, rt2, 5, 100);
        topology.addConnection(rt2, pc2, 10, 100);
        topology.addConnection(pc1, rt2, 50, 100);  // atalho caro

        rt1.deactivate();  // RT1 está inativo

        List<NetworkDevice> path = service.findPath(topology, pc1, pc2);

        // Deve ir PC1 → RT2 → PC2 (evitando RT1 inativo)
        assertFalse(path.contains(rt1), "Caminho não deve incluir dispositivo inativo");
    }

    @Test
    @DisplayName("updateAllRoutingTables deve preencher tabelas dos roteadores")
    void shouldUpdateRoutingTables() {
        topology.addConnection(pc1, rt1, 10, 100);
        topology.addConnection(rt1, rt2, 5, 100);
        topology.addConnection(rt2, pc2, 10, 100);

        service.updateAllRoutingTables(topology);

        assertFalse(rt1.getRoutingTable().isEmpty(), "RT1 deve ter rotas calculadas");
        assertFalse(rt2.getRoutingTable().isEmpty(), "RT2 deve ter rotas calculadas");
    }
}