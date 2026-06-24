package com.netsim.domain.entity;

import com.netsim.domain.valueobject.PacketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa um pacote de rede (PDU — Protocol Data Unit).
 *
 * Conceito de redes: pacotes são unidades de dados fragmentadas
 * da mensagem original para transmissão na rede. Contêm cabeçalho
 * (metadados: IPs, TTL) e payload (dados do usuário).
 *
 * TTL (Time To Live): contador que decrementa a cada roteador
 * atravessado. Quando chega a 0, o pacote é descartado.
 * Isso evita que pacotes circulem infinitamente em loops de roteamento.
 *
 * Usa o padrão Builder para construção fluente e imutabilidade.
 */
public class Packet {

    private final String id;
    private final String sourceIp;
    private final String destinationIp;
    private final String payload;
    private int ttl;
    private final LocalDateTime timestamp;
    private PacketStatus status;

    private Packet(Builder builder) {
        this.id            = UUID.randomUUID().toString();
        this.sourceIp      = builder.sourceIp;
        this.destinationIp = builder.destinationIp;
        this.payload       = builder.payload;
        this.ttl           = builder.ttl;
        this.timestamp     = LocalDateTime.now();
        this.status        = PacketStatus.IN_TRANSIT;
    }

    // --- Comportamento de domínio ---

    /** Decrementa o TTL. Retorna true se o pacote ainda pode ser roteado. */
    public boolean decrementTtl() {
        ttl--;
        if (ttl <= 0) {
            this.status = PacketStatus.TTL_EXPIRED;
            return false;
        }
        return true;
    }

    public void markDelivered() {
        this.status = PacketStatus.DELIVERED;
    }

    public void markDropped() {
        this.status = PacketStatus.DROPPED;
    }

    public boolean isAlive() {
        return status == PacketStatus.IN_TRANSIT;
    }

    // --- Getters ---

    public String getId()            { return id; }
    public String getSourceIp()      { return sourceIp; }
    public String getDestinationIp() { return destinationIp; }
    public String getPayload()       { return payload; }
    public int getTtl()              { return ttl; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public PacketStatus getStatus()  { return status; }

    @Override
    public String toString() {
        return String.format("Packet[%s → %s | TTL=%d | %s]",
                sourceIp, destinationIp, ttl, status);
    }

    // --- Builder (Design Pattern) ---

    public static class Builder {
        private String sourceIp;
        private String destinationIp;
        private String payload  = "";
        private int ttl         = 64;

        public Builder sourceIp(String sourceIp) {
            this.sourceIp = sourceIp;
            return this;
        }

        public Builder destinationIp(String destinationIp) {
            this.destinationIp = destinationIp;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder ttl(int ttl) {
            if (ttl <= 0 || ttl > 255) throw new IllegalArgumentException("TTL deve estar entre 1 e 255.");
            this.ttl = ttl;
            return this;
        }

        public Packet build() {
            if (sourceIp == null || destinationIp == null) {
                throw new IllegalStateException("sourceIp e destinationIp são obrigatórios.");
            }
            return new Packet(this);
        }
    }
}