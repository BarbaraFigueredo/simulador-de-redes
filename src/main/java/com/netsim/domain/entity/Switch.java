package com.netsim.domain.entity;

import com.netsim.domain.valueobject.DeviceType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Switch — comutador de camada 2 (enlace) do modelo OSI.
 *
 * Conceito de redes: switches encaminham quadros Ethernet baseando-se
 * em endereços MAC (não IP). Mantém uma tabela CAM (Content Addressable
 * Memory) que mapeia endereço MAC → porta.
 *
 * Diferença entre Switch e Roteador:
 *  - Switch: camada 2, trabalha dentro da mesma rede (broadcast domain)
 *  - Roteador: camada 3, conecta redes diferentes
 *
 * Nesta simulação simplificada, o switch encaminha baseado em IP
 * (para fins didáticos), registrando o mapeamento ip → device-id.
 */
public class Switch extends NetworkDevice {

    /** Tabela de comutação: IP/MAC → ID do dispositivo de destino. */
    private final Map<String, String> macTable = new HashMap<>();

    public Switch(String name, String ipAddress) {
        super(name, ipAddress, DeviceType.SWITCH);
    }

    @Override
    public void sendPacket(Packet packet) {
        if (!isActive()) return;
        addToHistory(packet);
    }

    /**
     * Recebe um pacote, aprende o mapeamento de origem e encaminha.
     * Switches NÃO decrementam TTL (operação de camada 2).
     */
    @Override
    public void receivePacket(Packet packet) {
        if (!isActive()) {
            packet.markDropped();
            return;
        }
        learnAddress(packet.getSourceIp());
        addToHistory(packet);
    }

    /**
     * Aprende o endereço de origem na tabela de comutação.
     * Simula o aprendizado dinâmico de MAC address dos switches reais.
     */
    private void learnAddress(String ipAddress) {
        macTable.putIfAbsent(ipAddress, ipAddress);
    }

    /**
     * Verifica se o switch conhece o destino (já aprendeu o endereço).
     * Se não, faz flooding (envia para todas as portas — não modelado aqui).
     */
    public boolean forwardPacket(Packet packet) {
        return macTable.containsKey(packet.getDestinationIp());
    }

    public Map<String, String> getMacTable() {
        return Collections.unmodifiableMap(macTable);
    }

    public void clearMacTable() {
        macTable.clear();
    }
}