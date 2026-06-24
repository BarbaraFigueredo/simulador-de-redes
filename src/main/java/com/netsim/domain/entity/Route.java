package com.netsim.domain.entity;

import java.util.Objects;

/**
 * Representa uma entrada na tabela de roteamento.
 *
 * Conceito de redes: tabelas de roteamento mapeiam redes de destino
 * ao próximo salto (next-hop), com um custo associado (métrica).
 * Roteadores consultam essa tabela para decidir para onde encaminhar
 * cada pacote recebido.
 *
 * Exemplo:
 *   Rede destino: 192.168.2.0/24
 *   Next-hop:     192.168.1.1  (endereço do próximo roteador)
 *   Custo:        10 ms (latência)
 */
public class Route {

    private final String destinationNetwork;
    private final String nextHop;
    private final int cost;

    public Route(String destinationNetwork, String nextHop, int cost) {
        if (destinationNetwork == null || destinationNetwork.isBlank())
            throw new IllegalArgumentException("Rede de destino é obrigatória.");
        if (nextHop == null || nextHop.isBlank())
            throw new IllegalArgumentException("Next-hop é obrigatório.");
        if (cost < 0)
            throw new IllegalArgumentException("Custo não pode ser negativo.");

        this.destinationNetwork = destinationNetwork;
        this.nextHop            = nextHop;
        this.cost               = cost;
    }

    public String getDestinationNetwork() { return destinationNetwork; }
    public String getNextHop()            { return nextHop; }
    public int getCost()                  { return cost; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Route)) return false;
        Route r = (Route) o;
        return cost == r.cost
                && Objects.equals(destinationNetwork, r.destinationNetwork)
                && Objects.equals(nextHop, r.nextHop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destinationNetwork, nextHop, cost);
    }

    @Override
    public String toString() {
        return String.format("Route[dest=%s via %s custo=%d]", destinationNetwork, nextHop, cost);
    }
}