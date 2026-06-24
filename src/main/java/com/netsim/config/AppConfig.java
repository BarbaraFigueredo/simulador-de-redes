package com.netsim.config;

import com.netsim.application.SimulationStatistics;
import com.netsim.application.usecase.*;
import com.netsim.domain.entity.NetworkTopology;
import com.netsim.domain.service.DijkstraRoutingService;
import com.netsim.domain.service.SimulationService;
import com.netsim.domain.service.TopologyService;

/**
 * Configuração central da aplicação — equivalente a um container DI simples.
 *
 * Princípio DIP: as dependências são injetadas aqui e não espalhadas pela app.
 * Em um projeto maior, substitua por Spring DI ou Guice.
 *
 * Singleton manual (não usa enum para permitir reset em testes).
 */
public class AppConfig {

    private static AppConfig instance;

    private final NetworkTopology        topology;
    private final DijkstraRoutingService routingService;
    private final TopologyService        topologyService;
    private final SimulationService.SimulationConfig simConfig;
    private final SimulationService      simulationService;
    private final SimulationStatistics   statistics;

    private final AddDeviceUseCase      addDeviceUseCase;
    private final RemoveDeviceUseCase   removeDeviceUseCase;
    private final ConnectDevicesUseCase connectDevicesUseCase;
    private final SendPacketUseCase     sendPacketUseCase;
    private final ExportTopologyUseCase exportTopologyUseCase;
    private final ImportTopologyUseCase importTopologyUseCase;

    private AppConfig() {
        topology        = new NetworkTopology();
        routingService  = new DijkstraRoutingService();
        topologyService = new TopologyService(topology, routingService);
        simConfig       = new SimulationService.SimulationConfig();
        simulationService = new SimulationService(topologyService, routingService, simConfig);
        statistics      = new SimulationStatistics();

        addDeviceUseCase      = new AddDeviceUseCase(topologyService);
        removeDeviceUseCase   = new RemoveDeviceUseCase(topologyService);
        connectDevicesUseCase = new ConnectDevicesUseCase(topologyService);
        sendPacketUseCase     = new SendPacketUseCase(simulationService, statistics);
        exportTopologyUseCase = new ExportTopologyUseCase(topology);
        importTopologyUseCase = new ImportTopologyUseCase(topology, routingService);
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) instance = new AppConfig();
        return instance;
    }

    public NetworkTopology getTopology()                 { return topology; }
    public TopologyService getTopologyService()          { return topologyService; }
    public SimulationService.SimulationConfig getSimConfig() { return simConfig; }
    public SimulationStatistics getStatistics()          { return statistics; }
    public AddDeviceUseCase getAddDeviceUseCase()        { return addDeviceUseCase; }
    public RemoveDeviceUseCase getRemoveDeviceUseCase()  { return removeDeviceUseCase; }
    public ConnectDevicesUseCase getConnectDevicesUseCase() { return connectDevicesUseCase; }
    public SendPacketUseCase getSendPacketUseCase()      { return sendPacketUseCase; }
    public ExportTopologyUseCase getExportTopologyUseCase() { return exportTopologyUseCase; }
    public ImportTopologyUseCase getImportTopologyUseCase() { return importTopologyUseCase; }
    public DijkstraRoutingService getRoutingService()    { return routingService; }
}