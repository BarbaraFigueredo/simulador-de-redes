package com.netsim.domain.entity;

import com.netsim.domain.valueobject.DeviceType;

/**
 * Computador — host final na rede (end device).
 *
 * Conceito de redes: computadores são os dispositivos geradores e
 * consumidores de tráfego. Eles iniciam conexões (clientes) ou
 * respondem a elas (servidores). Operam na camada de aplicação
 * mas também possuem camadas 3 (IP) e 4 (TCP/UDP).
 *
 * No modelo OSI, o computador é o único que processa todas as 7 camadas.
 */
public class Computer extends NetworkDevice {

    private String operatingSystem;

    public Computer(String name, String ipAddress) {
        super(name, ipAddress, DeviceType.COMPUTER);
        this.operatingSystem = "Linux";
    }

    public Computer(String name, String ipAddress, String operatingSystem) {
        super(name, ipAddress, DeviceType.COMPUTER);
        this.operatingSystem = operatingSystem;
    }

    @Override
    public void sendPacket(Packet packet) {
        if (!isActive()) return;
        addToHistory(packet);
    }

    @Override
    public void receivePacket(Packet packet) {
        if (!isActive()) {
            packet.markDropped();
            return;
        }
        packet.markDelivered();
        addToHistory(packet);
    }

    public String getOperatingSystem()            { return operatingSystem; }
    public void setOperatingSystem(String os)     { this.operatingSystem = os; }
}