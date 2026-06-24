package com.netsim.presentation.viewmodel;

import com.netsim.application.SimulationStatistics;
import com.netsim.application.event.EventBus;
import com.netsim.application.event.SimulationEventType;
import com.netsim.config.AppConfig;
import com.netsim.domain.entity.Connection;
import com.netsim.domain.entity.NetworkDevice;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Optional;

/**
 * ViewModel principal da aplicação.
 *
 * Publica propriedades observáveis que o controller assina via binding.
 * Não importa nada de JavaFX para fora desta camada — a binding é
 * responsabilidade do controller (MVVM).
 */
public class MainViewModel {

    private final AppConfig config;

    private final ObservableList<DeviceViewModel>  devices     = FXCollections.observableArrayList();
    private final ObservableList<Connection>       connections = FXCollections.observableArrayList();
    private final ObservableList<String>           logs        = FXCollections.observableArrayList();
    private final ObjectProperty<DeviceViewModel>  selected    = new SimpleObjectProperty<>();
    private final StringProperty                   statsText   = new SimpleStringProperty("Aguardando simulação...");
    private final BooleanProperty                  stepMode    = new SimpleBooleanProperty(false);

    public MainViewModel(AppConfig config) {
        this.config = config;
        subscribeToEvents();
        stepMode.addListener((obs, old, val) -> config.getSimConfig().setStepMode(val));
    }

    private void subscribeToEvents() {
        EventBus.INSTANCE.subscribe(SimulationEventType.DEVICE_ADDED, e -> Platform.runLater(() -> {
            if (e.getPayload() instanceof NetworkDevice d) {
                devices.add(new DeviceViewModel(d));
            }
            refreshStats();
        }));

        EventBus.INSTANCE.subscribe(SimulationEventType.DEVICE_REMOVED, e -> Platform.runLater(() -> {
            String removedId = (String) e.getPayload();
            devices.removeIf(vm -> vm.getId().equals(removedId));
            connections.setAll(config.getTopology().getAllConnections());
            refreshStats();
        }));

        EventBus.INSTANCE.subscribe(SimulationEventType.CONNECTION_ADDED, e -> Platform.runLater(() -> {
            if (e.getPayload() instanceof Connection c) {
                connections.add(c);
                refreshAllDevices();
            }
        }));

        EventBus.INSTANCE.subscribe(SimulationEventType.CONNECTION_REMOVED, e -> Platform.runLater(() ->
                connections.setAll(config.getTopology().getAllConnections())));

        EventBus.INSTANCE.subscribe(SimulationEventType.SIMULATION_LOG, e -> Platform.runLater(() -> {
            logs.add(0, e.toString());
            if (logs.size() > 200) logs.remove(200, logs.size());
        }));

        EventBus.INSTANCE.subscribe(SimulationEventType.PACKET_DELIVERED, e -> Platform.runLater(() -> {
            logs.add(0, e.toString());
            refreshStats();
        }));

        EventBus.INSTANCE.subscribe(SimulationEventType.PACKET_DROPPED, e -> Platform.runLater(() -> {
            logs.add(0, e.toString());
            refreshStats();
        }));

        EventBus.INSTANCE.subscribe(SimulationEventType.TOPOLOGY_LOADED, e -> Platform.runLater(() -> {
            rebuildFromTopology();
            refreshStats();
        }));
    }

    public void rebuildFromTopology() {
        devices.clear();
        config.getTopology().getAllDevices()
                .forEach(d -> devices.add(new DeviceViewModel(d)));
        connections.setAll(config.getTopology().getAllConnections());
    }

    private void refreshAllDevices() {
        devices.forEach(DeviceViewModel::refresh);
    }

    private void refreshStats() {
        SimulationStatistics s = config.getStatistics();
        Platform.runLater(() -> statsText.set(s.toString()));
    }

    public void selectDevice(String deviceId) {
        devices.stream().filter(vm -> vm.getId().equals(deviceId))
                .findFirst().ifPresent(vm -> {
                    vm.refresh();
                    selected.set(vm);
                });
    }

    public void clearLogs() {
        logs.clear();
    }

    public void resetStats() {
        config.getStatistics().reset();
        refreshStats();
    }

    // Accessors
    public ObservableList<DeviceViewModel>  getDevices()     { return devices; }
    public ObservableList<Connection>       getConnections() { return connections; }
    public ObservableList<String>           getLogs()        { return logs; }
    public ObjectProperty<DeviceViewModel>  selectedProperty() { return selected; }
    public StringProperty statsTextProperty()                { return statsText; }
    public BooleanProperty stepModeProperty()                { return stepMode; }
    public AppConfig getConfig()                             { return config; }

    public Optional<DeviceViewModel> findViewModelByIp(String ip) {
        return devices.stream().filter(vm -> vm.getDevice().getIpAddress().equals(ip)).findFirst();
    }
}