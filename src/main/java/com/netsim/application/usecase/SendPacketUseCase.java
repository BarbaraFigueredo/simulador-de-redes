package com.netsim.application.usecase;

import com.netsim.application.SimulationStatistics;
import com.netsim.application.event.EventBus;
import com.netsim.application.event.SimulationEventType;
import com.netsim.domain.entity.NetworkDevice;
import com.netsim.domain.entity.Packet;
import com.netsim.domain.service.SimulationService;

import java.util.List;
import java.util.function.Consumer;

/**
 * Caso de uso: enviar um pacote de um dispositivo para outro.
 *
 * Orquestra: criação do pacote, execução da simulação, coleta de estatísticas.
 * A animação visual é acionada via callback (onPathFound).
 */
public class SendPacketUseCase {

    private final SimulationService    simulationService;
    private final SimulationStatistics statistics;

    public SendPacketUseCase(SimulationService simulationService,
                             SimulationStatistics statistics) {
        this.simulationService = simulationService;
        this.statistics        = statistics;
    }

    /**
     * @param sourceIp       IP de origem
     * @param destinationIp  IP de destino
     * @param payload        Conteúdo do pacote
     * @param ttl            Time To Live
     * @param onPathFound    Callback com o caminho para animação na UI
     */
    public void execute(String sourceIp, String destinationIp,
                        String payload, int ttl,
                        Consumer<List<NetworkDevice>> onPathFound) {

        Packet packet = new Packet.Builder()
                .sourceIp(sourceIp)
                .destinationIp(destinationIp)
                .payload(payload)
                .ttl(ttl)
                .build();

        statistics.recordSent();
        EventBus.INSTANCE.publish(SimulationEventType.PACKET_SENT,
                String.format("%s → %s (TTL=%d)", sourceIp, destinationIp, ttl),
                packet);

        SimulationService.SimulationResult result = simulationService.simulate(
                packet,
                message -> EventBus.INSTANCE.publish(SimulationEventType.SIMULATION_LOG, message),
                onPathFound
        );

        if (result.isDelivered()) {
            statistics.recordDelivered(result.getTotalLatencyMs());
            EventBus.INSTANCE.publish(SimulationEventType.PACKET_DELIVERED,
                    String.format("Pacote entregue em %s → %s | %dms",
                            sourceIp, destinationIp, result.getTotalLatencyMs()));
        } else {
            statistics.recordDropped();
            EventBus.INSTANCE.publish(SimulationEventType.PACKET_DROPPED,
                    String.format("Pacote perdido: %s → %s", sourceIp, destinationIp));
        }
    }
}