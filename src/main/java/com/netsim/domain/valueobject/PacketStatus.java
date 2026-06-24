package com.netsim.domain.valueobject;

/**
 * Status de ciclo de vida de um pacote de rede.
 *
 * IN_TRANSIT  : pacote sendo roteado pelos nós
 * DELIVERED   : pacote chegou ao destino com sucesso
 * DROPPED     : descartado por congestionamento ou política
 * TTL_EXPIRED : TTL (Time To Live) chegou a zero — evita loops infinitos
 */
public enum PacketStatus {
    IN_TRANSIT,
    DELIVERED,
    DROPPED,
    TTL_EXPIRED
}