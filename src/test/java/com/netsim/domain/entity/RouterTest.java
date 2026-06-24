package com.netsim.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Router — Tabela de Roteamento")
class RouterTest {

    private Router router;

    @BeforeEach
    void setUp() {
        router = new Router("RT-01", "10.0.0.1");
    }

    @Test
    @DisplayName("Deve adicionar rota à tabela")
    void shouldAddRoute() {
        router.updateRoutes(new Route("192.168.2.0/24", "10.0.0.2", 10));

        assertFalse(router.getRoutingTable().isEmpty());
        assertEquals(1, router.getRoutingTable().size());
    }

    @Test
    @DisplayName("Deve substituir rota existente para a mesma rede de destino")
    void shouldReplaceExistingRoute() {
        router.updateRoutes(new Route("192.168.2.0/24", "10.0.0.2", 10));
        router.updateRoutes(new Route("192.168.2.0/24", "10.0.0.3", 5));

        assertEquals(1, router.getRoutingTable().size(), "Deve ter apenas 1 rota para a rede");
        assertEquals("10.0.0.3", router.getRoutingTable().get(0).getNextHop());
    }

    @Test
    @DisplayName("Deve decrementar TTL ao receber pacote")
    void shouldDecrementTtlOnReceive() {
        Packet packet = new Packet.Builder()
                .sourceIp("192.168.1.1")
                .destinationIp("192.168.2.1")
                .ttl(64)
                .build();

        router.receivePacket(packet);

        assertEquals(63, packet.getTtl(), "TTL deve ser decrementado em 1 pelo roteador");
    }

    @Test
    @DisplayName("Deve encontrar rota para o destino")
    void shouldRoutePacketToKnownNetwork() {
        router.updateRoutes(new Route("192.168.2.0/24", "10.0.0.2", 10));

        Packet packet = new Packet.Builder()
                .sourceIp("192.168.1.1")
                .destinationIp("192.168.2.50")
                .build();

        Optional<Route> route = router.routePacket(packet);

        assertTrue(route.isPresent(), "Deve encontrar rota para 192.168.2.x");
        assertEquals("10.0.0.2", route.get().getNextHop());
    }

    @Test
    @DisplayName("Deve descartar pacote quando está inativo")
    void shouldDropPacketWhenInactive() {
        router.deactivate();
        Packet packet = new Packet.Builder()
                .sourceIp("192.168.1.1")
                .destinationIp("192.168.2.1")
                .ttl(10)
                .build();

        router.receivePacket(packet);

        assertEquals(com.netsim.domain.valueobject.PacketStatus.DROPPED, packet.getStatus());
    }
}