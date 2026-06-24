package com.netsim.domain.service;

import com.netsim.domain.entity.NetworkDevice;
import com.netsim.domain.entity.Packet;
import com.netsim.domain.valueobject.PacketStatus;

import java.util.List;
import java.util.function.Consumer;

/**
 * Serviço de domínio que orquestra a simulação do envio de um pacote.
 *
 * Usa um callback (Consumer) para notificar cada passo da simulação.
 * Isso permite que a camada de apresentação receba eventos sem
 * acoplamento direto (Observer / callback pattern).
 *
 * Conceito de redes: cada passo representa um "hop" (salto).
 * Um traceroute real mostra exatamente esses saltos com RTTs.
 */
public class SimulationService {

    private final TopologyService    topologyService;
    private final RoutingStrategy    routingStrategy;
    private final SimulationConfig   config;

    public SimulationService(TopologyService topologyService,
                             RoutingStrategy routingStrategy,
                             SimulationConfig config) {
        this.topologyService = topologyService;
        this.routingStrategy = routingStrategy;
        this.config          = config;
    }

    /**
     * Simula o envio de um pacote pela rede.
     *
     * @param packet       Pacote a ser transmitido
     * @param onStep       Callback chamado a cada hop com a descrição do evento
     * @param onPathFound  Callback com o caminho completo (para animação)
     */
    public SimulationResult simulate(Packet packet,
                                     Consumer<String> onStep,
                                     Consumer<List<NetworkDevice>> onPathFound) {
        SimulationResult result = new SimulationResult();

        NetworkDevice source = topologyService.findByIp(packet.getSourceIp())
                .orElse(null);
        NetworkDevice destination = topologyService.findByIp(packet.getDestinationIp())
                .orElse(null);

        if (source == null) {
            onStep.accept("[ERRO] Origem não encontrada: " + packet.getSourceIp());
            result.setDropped(true);
            return result;
        }
        if (destination == null) {
            onStep.accept("[ERRO] Destino não encontrado: " + packet.getDestinationIp());
            result.setDropped(true);
            return result;
        }
        if (!source.isActive()) {
            onStep.accept("[ERRO] Dispositivo de origem está inativo: " + source.getName());
            result.setDropped(true);
            return result;
        }

        List<NetworkDevice> path = routingStrategy.findPath(
                topologyService.getTopology(), source, destination);

        if (path.isEmpty()) {
            onStep.accept("[DESCARTADO] Sem rota para " + packet.getDestinationIp());
            packet.markDropped();
            result.setDropped(true);
            return result;
        }

        onPathFound.accept(path);
        onStep.accept(String.format("[ENVIO] %s → %s | Caminho: %d saltos | TTL=%d",
                source.getName(), destination.getName(), path.size() - 1, packet.getTtl()));

        source.sendPacket(packet);
        long startTime = System.currentTimeMillis();

        // Percorre cada hop do caminho
        for (int i = 1; i < path.size(); i++) {
            NetworkDevice hop = path.get(i);

            if (!packet.isAlive()) break;

            // Simula latência do enlace
            result.addLatency(getLatencyForHop(path.get(i - 1), hop));

            hop.receivePacket(packet);

            if (packet.getStatus() == PacketStatus.TTL_EXPIRED) {
                onStep.accept(String.format("[TTL=0] Pacote expirou em %s", hop.getName()));
                result.setDropped(true);
                return result;
            }

            if (!hop.isActive()) {
                onStep.accept(String.format("[ERRO] %s está inativo — pacote descartado", hop.getName()));
                packet.markDropped();
                result.setDropped(true);
                return result;
            }

            if (i < path.size() - 1) {
                onStep.accept(String.format("[HOP %d] %s encaminhou pacote → %s | TTL=%d",
                        i, hop.getName(), path.get(i + 1).getName(), packet.getTtl()));
            }
        }

        if (packet.getStatus() == PacketStatus.DELIVERED) {
            long elapsed = System.currentTimeMillis() - startTime;
            onStep.accept(String.format("[ENTREGUE] Pacote chegou a %s | Latência simulada: %dms",
                    destination.getName(), result.getTotalLatencyMs()));
            result.setDelivered(true);
        }

        return result;
    }

    private int getLatencyForHop(NetworkDevice from, NetworkDevice to) {
        return topologyService.getTopology()
                .getConnection(from, to)
                .map(c -> c.getLatencyMs())
                .orElse(10);
    }

    // --- Resultado imutável da simulação ---

    public static class SimulationResult {
        private boolean delivered   = false;
        private boolean dropped     = false;
        private int totalLatencyMs  = 0;

        public void setDelivered(boolean v)   { this.delivered = v; }
        public void setDropped(boolean v)     { this.dropped = v; }
        public void addLatency(int ms)        { this.totalLatencyMs += ms; }
        public boolean isDelivered()          { return delivered; }
        public boolean isDropped()            { return dropped; }
        public int getTotalLatencyMs()        { return totalLatencyMs; }
    }

    // --- Configuração injetável ---

    public static class SimulationConfig {
        private double packetLossProbability = 0.0;
        private boolean stepMode             = false;

        public double getPacketLossProbability()         { return packetLossProbability; }
        public void setPacketLossProbability(double p)   { this.packetLossProbability = p; }
        public boolean isStepMode()                      { return stepMode; }
        public void setStepMode(boolean stepMode)        { this.stepMode = stepMode; }
    }
}