package com.netsim.domain.entity;

import com.netsim.domain.valueobject.DeviceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Roteador — dispositivo de camada 3 (rede) do modelo OSI.
 *
 * Conceito de redes: roteadores tomam decisões de encaminhamento
 * consultando a tabela de roteamento (routing table). Cada entrada
 * mapeia uma rede de destino ao próximo salto (next-hop) com um custo.
 *
 * O algoritmo de Dijkstra (DijkstraRoutingService) preenche e atualiza
 * essa tabela automaticamente com base na topologia.
 *
 * Funções principais do roteador:
 *  1. Receber pacote
 *  2. Decrementar TTL
 *  3. Consultar tabela de roteamento
 *  4. Encaminhar ao próximo salto
 */
public class Router extends NetworkDevice {

    private final List<Route> routingTable = new ArrayList<>();

    public Router(String name, String ipAddress) {
        super(name, ipAddress, DeviceType.ROUTER);
    }

    @Override
    public void sendPacket(Packet packet) {
        if (!isActive()) return;
        addToHistory(packet);
    }

    /**
     * Recebe um pacote, decrementa o TTL e registra no histórico.
     * O encaminhamento real é orquestrado pelo SimulationService.
     */
    @Override
    public void receivePacket(Packet packet) {
        if (!isActive()) {
            packet.markDropped();
            return;
        }
        packet.decrementTtl();
        addToHistory(packet);
    }

    /**
     * Consulta a tabela de roteamento para encontrar o next-hop
     * para a rede de destino do pacote.
     */
    public Optional<Route> routePacket(Packet packet) {
        return routingTable.stream()
                .filter(r -> packet.getDestinationIp().startsWith(
                        r.getDestinationNetwork().replace(".0/24", "")))
                .min((a, b) -> Integer.compare(a.getCost(), b.getCost()));
    }

    /** Insere ou atualiza uma rota na tabela. */
    public void updateRoutes(Route route) {
        routingTable.removeIf(r ->
                r.getDestinationNetwork().equals(route.getDestinationNetwork()));
        routingTable.add(route);
    }

    public void clearRoutingTable() {
        routingTable.clear();
    }

    public List<Route> getRoutingTable() {
        return Collections.unmodifiableList(routingTable);
    }
}