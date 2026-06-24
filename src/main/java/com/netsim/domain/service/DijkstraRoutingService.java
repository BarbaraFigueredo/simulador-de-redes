package com.netsim.domain.service;

import com.netsim.domain.entity.Connection;
import com.netsim.domain.entity.NetworkDevice;
import com.netsim.domain.entity.NetworkTopology;
import com.netsim.domain.entity.Route;
import com.netsim.domain.entity.Router;

import java.util.*;

/**
 * Implementa o algoritmo de Dijkstra para roteamento de menor custo.
 *
 * ─── Como funciona o Dijkstra ───────────────────────────────────────
 * Dado um grafo com pesos nas arestas, o algoritmo encontra o caminho
 * de menor custo total do nó origem a todos os outros nós.
 *
 * Passos:
 *  1. Inicializa dist[origem] = 0, dist[outros] = ∞
 *  2. Fila de prioridade ordenada por dist crescente
 *  3. Enquanto a fila não estiver vazia:
 *     a. Extrai nó U com menor dist
 *     b. Para cada vizinho V de U:
 *        - novaDist = dist[U] + peso(U,V)
 *        - Se novaDist < dist[V]: atualiza dist[V] e anterior[V] = U
 *  4. Reconstrói o caminho seguindo os anteriores do destino à origem
 *
 * Complexidade: O((V + E) log V) com heap binário
 *
 * Correspondência com redes reais: OSPF (RFC 2328) usa Dijkstra (SPF)
 * para calcular a árvore de menor caminho (SPT — Shortest Path Tree).
 * ────────────────────────────────────────────────────────────────────
 */
public class DijkstraRoutingService implements RoutingStrategy {

    private static final int INFINITY = Integer.MAX_VALUE / 2;

    @Override
    public List<NetworkDevice> findPath(NetworkTopology topology,
                                        NetworkDevice source,
                                        NetworkDevice target) {
        if (source.equals(target)) return List.of(source);

        Map<String, Integer>       dist     = new HashMap<>();
        Map<String, NetworkDevice> previous = new HashMap<>();
        Set<String>                visited  = new HashSet<>();

        // Fila de prioridade: (custo, dispositivo)
        PriorityQueue<Map.Entry<Integer, NetworkDevice>> pq =
                new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getKey));

        // Inicialização: todos com distância infinita
        for (NetworkDevice device : topology.getAllDevices()) {
            dist.put(device.getId(), INFINITY);
        }
        dist.put(source.getId(), 0);
        pq.offer(Map.entry(0, source));

        // Relaxamento de arestas
        while (!pq.isEmpty()) {
            Map.Entry<Integer, NetworkDevice> current = pq.poll();
            NetworkDevice u = current.getValue();

            if (visited.contains(u.getId())) continue;
            visited.add(u.getId());

            if (u.equals(target)) break;

            for (NetworkDevice neighbor : topology.getNeighbors(u)) {
                if (visited.contains(neighbor.getId())) continue;
                if (!neighbor.isActive()) continue;

                Optional<Connection> conn = topology.getConnection(u, neighbor);
                int weight = conn.map(Connection::getLatencyMs).orElse(1);

                int newDist = dist.get(u.getId()) + weight;
                if (newDist < dist.get(neighbor.getId())) {
                    dist.put(neighbor.getId(), newDist);
                    previous.put(neighbor.getId(), u);
                    pq.offer(Map.entry(newDist, neighbor));
                }
            }
        }

        return reconstructPath(previous, source, target);
    }

    /** Reconstrói o caminho seguindo o mapa de predecessores. */
    private List<NetworkDevice> reconstructPath(Map<String, NetworkDevice> previous,
                                                 NetworkDevice source,
                                                 NetworkDevice target) {
        LinkedList<NetworkDevice> path = new LinkedList<>();
        NetworkDevice current = target;

        while (current != null && !current.equals(source)) {
            path.addFirst(current);
            current = previous.get(current.getId());
        }

        if (current == null) return Collections.emptyList(); // sem caminho

        path.addFirst(source);
        return new ArrayList<>(path);
    }

    /**
     * Popula a tabela de roteamento de todos os roteadores na topologia.
     * Executado após qualquer mudança na topologia.
     */
    public void updateAllRoutingTables(NetworkTopology topology) {
        for (NetworkDevice device : topology.getAllDevices()) {
            if (!(device instanceof Router router)) continue;

            router.clearRoutingTable();

            for (NetworkDevice target : topology.getAllDevices()) {
                if (target.equals(device)) continue;

                List<NetworkDevice> path = findPath(topology, device, target);
                if (path.size() < 2) continue;

                NetworkDevice nextHop = path.get(1);
                int totalCost = computePathCost(topology, path);

                String destNetwork = target.getIpAddress()
                        .replaceAll("(\\d+\\.\\d+\\.\\d+)\\.\\d+", "$1.0/24");

                router.updateRoutes(new Route(destNetwork, nextHop.getIpAddress(), totalCost));
            }
        }
    }

    private int computePathCost(NetworkTopology topology, List<NetworkDevice> path) {
        int total = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Optional<Connection> conn = topology.getConnection(path.get(i), path.get(i + 1));
            total += conn.map(Connection::getLatencyMs).orElse(1);
        }
        return total;
    }
}