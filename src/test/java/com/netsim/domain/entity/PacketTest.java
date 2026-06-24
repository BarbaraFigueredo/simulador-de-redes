package com.netsim.domain.entity;

import com.netsim.domain.valueobject.PacketStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Packet — Ciclo de vida e TTL")
class PacketTest {

    @Test
    @DisplayName("Deve criar pacote com status IN_TRANSIT")
    void shouldCreateWithInTransitStatus() {
        Packet p = new Packet.Builder()
                .sourceIp("10.0.0.1")
                .destinationIp("10.0.0.2")
                .ttl(5)
                .build();

        assertEquals(PacketStatus.IN_TRANSIT, p.getStatus());
        assertEquals(5, p.getTtl());
    }

    @Test
    @DisplayName("Deve expirar quando TTL chega a zero")
    void shouldExpireWhenTtlReachesZero() {
        Packet p = new Packet.Builder()
                .sourceIp("10.0.0.1")
                .destinationIp("10.0.0.2")
                .ttl(1)
                .build();

        boolean alive = p.decrementTtl();

        assertFalse(alive, "Pacote com TTL=1 após decremento deve estar morto");
        assertEquals(PacketStatus.TTL_EXPIRED, p.getStatus());
    }

    @Test
    @DisplayName("Deve marcar como DELIVERED ao chegar ao destino")
    void shouldMarkAsDelivered() {
        Packet p = new Packet.Builder()
                .sourceIp("10.0.0.1")
                .destinationIp("10.0.0.2")
                .build();

        p.markDelivered();

        assertEquals(PacketStatus.DELIVERED, p.getStatus());
        assertFalse(p.isAlive());
    }

    @Test
    @DisplayName("Deve lançar exceção para TTL inválido")
    void shouldThrowForInvalidTtl() {
        assertThrows(IllegalArgumentException.class, () ->
                new Packet.Builder()
                        .sourceIp("10.0.0.1")
                        .destinationIp("10.0.0.2")
                        .ttl(0)
                        .build());
    }

    @Test
    @DisplayName("Deve lançar exceção quando IP de origem não informado")
    void shouldThrowWhenSourceIpMissing() {
        assertThrows(IllegalStateException.class, () ->
                new Packet.Builder()
                        .destinationIp("10.0.0.2")
                        .build());
    }
}