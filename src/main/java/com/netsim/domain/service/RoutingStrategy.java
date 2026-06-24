package com.netsim.domain.service;

import com.netsim.domain.entity.NetworkDevice;
import com.netsim.domain.entity.NetworkTopology;

import java.util.List;

/**
 * Strategy Pattern: define o contrato para algoritmos de roteamento.
 *
 * Permite trocar o algoritmo sem alterar o código que o usa.
 * Implementações possíveis: Dijkstra, Bellman-Ford, RIP (hop count), etc.
 */
public interface RoutingStrategy {

    /**
     * Encontra o melhor caminho entre origem e destino.
     *
     * @param topology  Grafo da rede
     * @param source    Dispositivo de origem
     * @param target    Dispositivo de destino
     * @return Lista de dispositivos formando o caminho (inclui origem e destino).
     *         Lista vazia se não houver caminho.
     */
    List<NetworkDevice> findPath(NetworkTopology topology,
                                 NetworkDevice source,
                                 NetworkDevice target);
}