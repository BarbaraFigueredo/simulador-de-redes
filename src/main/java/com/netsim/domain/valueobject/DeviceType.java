package com.netsim.domain.valueobject;

/**
 * Tipo do dispositivo de rede.
 *
 * COMPUTER : host final (camada 3 do modelo OSI)
 * SWITCH   : comutador de quadros Ethernet (camada 2)
 * ROUTER   : roteador IP (camada 3), mantém tabela de roteamento
 */
public enum DeviceType {
    COMPUTER,
    SWITCH,
    ROUTER
}