package com.netsim.domain.entity;

import com.netsim.domain.valueobject.DeviceStatus;
import com.netsim.domain.valueobject.DeviceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade base abstrata para qualquer dispositivo de rede.
 *
 * Aplica o princípio OCP (Open/Closed): aberta para extensão
 * (subclasses Computer, Router, Switch) e fechada para modificação.
 *
 * Aplica o princípio LSP (Liskov Substitution): qualquer subclasse
 * pode ser usada onde NetworkDevice é esperado sem quebrar o contrato.
 *
 * Usa Template Method Pattern: sendPacket() e receivePacket() definem
 * o esqueleto do algoritmo; as subclasses implementam os detalhes.
 */
public abstract class NetworkDevice {

    protected final String id;
    protected String name;
    protected String ipAddress;
    protected DeviceStatus status;
    protected final DeviceType type;

    /** Posição visual no canvas (coordenadas em pixels). */
    protected double positionX;
    protected double positionY;

    /** Histórico de pacotes processados por este dispositivo. */
    protected final List<Packet> packetHistory = new ArrayList<>();

    protected NetworkDevice(String name, String ipAddress, DeviceType type) {
        this.id         = UUID.randomUUID().toString();
        this.name       = Objects.requireNonNull(name, "Nome não pode ser nulo");
        this.ipAddress  = Objects.requireNonNull(ipAddress, "IP não pode ser nulo");
        this.type       = Objects.requireNonNull(type, "Tipo não pode ser nulo");
        this.status     = DeviceStatus.ACTIVE;
        this.positionX  = 100 + Math.random() * 400;
        this.positionY  = 100 + Math.random() * 300;
    }

    // --- Contrato abstrato (Template Method) ---

    /** Inicia o envio de um pacote a partir deste dispositivo. */
    public abstract void sendPacket(Packet packet);

    /** Processa um pacote recebido. */
    public abstract void receivePacket(Packet packet);

    // --- Comportamento compartilhado ---

    public void addToHistory(Packet packet) {
        packetHistory.add(packet);
    }

    public boolean isActive() {
        return status == DeviceStatus.ACTIVE;
    }

    public void activate()   { this.status = DeviceStatus.ACTIVE; }
    public void deactivate() { this.status = DeviceStatus.INACTIVE; }
    public void setError()   { this.status = DeviceStatus.ERROR; }

    // --- Getters / Setters ---

    public String getId()         { return id; }
    public String getName()       { return name; }
    public void setName(String n) { this.name = n; }
    public String getIpAddress()  { return ipAddress; }
    public void setIpAddress(String ip) { this.ipAddress = ip; }
    public DeviceStatus getStatus()     { return status; }
    public DeviceType getType()         { return type; }
    public double getPositionX()        { return positionX; }
    public double getPositionY()        { return positionY; }
    public void setPositionX(double x)  { this.positionX = x; }
    public void setPositionY(double y)  { this.positionY = y; }
    public List<Packet> getPacketHistory() { return List.copyOf(packetHistory); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetworkDevice)) return false;
        return id.equals(((NetworkDevice) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s[%s | %s | %s]", getClass().getSimpleName(), name, ipAddress, status);
    }
}