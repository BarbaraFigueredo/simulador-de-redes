package com.netsim.presentation.viewmodel;

import com.netsim.domain.entity.Computer;
import com.netsim.domain.entity.NetworkDevice;
import com.netsim.domain.entity.Route;
import com.netsim.domain.entity.Router;
import com.netsim.domain.entity.Switch;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * ViewModel que expõe as propriedades observáveis de um NetworkDevice.
 * A UI faz binding diretamente, sem acessar a entidade de domínio.
 */
public class DeviceViewModel {

    private final NetworkDevice device;

    private final StringProperty  name       = new SimpleStringProperty();
    private final StringProperty  ipAddress  = new SimpleStringProperty();
    private final StringProperty  type       = new SimpleStringProperty();
    private final StringProperty  status     = new SimpleStringProperty();
    private final StringProperty  extra      = new SimpleStringProperty();
    private final DoubleProperty  positionX  = new SimpleDoubleProperty();
    private final DoubleProperty  positionY  = new SimpleDoubleProperty();
    private final ObservableList<String> routes = FXCollections.observableArrayList();

    public DeviceViewModel(NetworkDevice device) {
        this.device = device;
        refresh();
    }

    public void refresh() {
        name.set(device.getName());
        ipAddress.set(device.getIpAddress());
        type.set(device.getType().name());
        status.set(device.getStatus().name());
        positionX.set(device.getPositionX());
        positionY.set(device.getPositionY());

        if (device instanceof Computer c) {
            extra.set("OS: " + c.getOperatingSystem());
        } else if (device instanceof Router r) {
            extra.set("Rotas: " + r.getRoutingTable().size());
            refreshRoutes(r.getRoutingTable());
        } else if (device instanceof Switch s) {
            extra.set("MACs aprendidos: " + s.getMacTable().size());
        }
    }

    private void refreshRoutes(List<Route> routeList) {
        routes.clear();
        routeList.forEach(r ->
                routes.add(String.format("%-20s via %-15s custo=%d",
                        r.getDestinationNetwork(), r.getNextHop(), r.getCost())));
    }

    public NetworkDevice getDevice()   { return device; }
    public String getId()              { return device.getId(); }
    public StringProperty nameProperty()      { return name; }
    public StringProperty ipAddressProperty() { return ipAddress; }
    public StringProperty typeProperty()      { return type; }
    public StringProperty statusProperty()    { return status; }
    public StringProperty extraProperty()     { return extra; }
    public DoubleProperty positionXProperty() { return positionX; }
    public DoubleProperty positionYProperty() { return positionY; }
    public ObservableList<String> getRoutes() { return routes; }

    @Override
    public String toString() { return device.getName() + " (" + device.getIpAddress() + ")"; }
}