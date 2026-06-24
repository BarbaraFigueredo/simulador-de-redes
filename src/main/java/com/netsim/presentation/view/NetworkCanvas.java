package com.netsim.presentation.view;

import com.netsim.domain.entity.Connection;
import com.netsim.domain.entity.NetworkDevice;
import com.netsim.presentation.viewmodel.DeviceViewModel;
import com.netsim.presentation.viewmodel.MainViewModel;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.*;
import java.util.function.Consumer;

/**
 * Canvas visual da rede — extensão de Pane que gerencia
 * DeviceNodeViews, ConnectionLineViews e animações de pacotes.
 *
 * Responsabilidades:
 *  1. Renderizar dispositivos e conexões
 *  2. Drag & drop de dispositivos
 *  3. Seleção de dispositivos (clique)
 *  4. Modo de conexão (clique em dois dispositivos)
 *  5. Delegar animação ao PacketAnimator
 */
public class NetworkCanvas extends Pane {

    private final MainViewModel viewModel;

    private final Map<String, DeviceNodeView>    nodeViews       = new LinkedHashMap<>();
    private final Map<String, ConnectionLineView> lineViews       = new LinkedHashMap<>();
    private final List<ConnectionLineView>        connectionLines = new ArrayList<>();
    private final PacketAnimator                  animator;

    private Consumer<DeviceViewModel>            onDeviceSelected;
    private Consumer<String[]>                   onConnectionRequest;
    private Runnable                             onFirstDeviceSelected;

    // Estado do drag
    private double dragStartX, dragStartY;

    // Estado do modo de conexão
    private boolean       connectMode        = false;
    private DeviceNodeView connectModeSource = null;
    private Line           connectPreviewLine;

    public NetworkCanvas(MainViewModel viewModel) {
        this.viewModel = viewModel;
        this.animator  = new PacketAnimator(this, nodeViews);

        setStyle("-fx-background-color: #1a1a2e;");
        setMinSize(600, 400);

        connectPreviewLine = new Line();
        connectPreviewLine.setStroke(Color.web("#ffd54f"));
        connectPreviewLine.setStrokeWidth(2);
        connectPreviewLine.getStrokeDashArray().addAll(10.0, 5.0);
        connectPreviewLine.setVisible(false);
        getChildren().add(connectPreviewLine);

        listenToViewModelChanges();
    }

    private void listenToViewModelChanges() {
        // Dispositivos adicionados
        viewModel.getDevices().addListener(
                (javafx.collections.ListChangeListener<DeviceViewModel>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(vm -> addDeviceNode(vm));
                }
                if (change.wasRemoved()) {
                    change.getRemoved().forEach(vm -> removeDeviceNode(vm.getId()));
                }
            }
        });

        // Conexões adicionadas
        viewModel.getConnections().addListener(
                (javafx.collections.ListChangeListener<Connection>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(this::addConnectionLine);
                }
                if (change.wasRemoved()) {
                    change.getRemoved().forEach(c -> removeConnectionLine(c.getId()));
                }
            }
        });
    }

    // --- Gestão de nós ---

    public void addDeviceNode(DeviceViewModel vm) {
        if (nodeViews.containsKey(vm.getId())) return;

        DeviceNodeView node = new DeviceNodeView(vm);
        nodeViews.put(vm.getId(), node);
        getChildren().add(node);

        setupDrag(node);
        setupClick(node);
    }

    private void removeDeviceNode(String deviceId) {
        DeviceNodeView node = nodeViews.remove(deviceId);
        if (node != null) getChildren().remove(node);
        connectionLines.removeIf(l -> {
            boolean match = l.getConnection().getDeviceA().getId().equals(deviceId)
                         || l.getConnection().getDeviceB().getId().equals(deviceId);
            if (match) getChildren().remove(l);
            return match;
        });
    }

    private void addConnectionLine(Connection connection) {
        DeviceNodeView nodeA = nodeViews.get(connection.getDeviceA().getId());
        DeviceNodeView nodeB = nodeViews.get(connection.getDeviceB().getId());
        if (nodeA == null || nodeB == null) return;

        ConnectionLineView lineView = new ConnectionLineView(connection, nodeA, nodeB);
        lineViews.put(connection.getId(), lineView);
        connectionLines.add(lineView);
        getChildren().add(0, lineView); // atrás dos nós
    }

    private void removeConnectionLine(String connectionId) {
        ConnectionLineView line = lineViews.remove(connectionId);
        if (line != null) {
            getChildren().remove(line);
            connectionLines.remove(line);
        }
    }

    // --- Drag & Drop ---

    private void setupDrag(DeviceNodeView node) {
        node.setOnMousePressed(e -> {
            if (connectMode) return;
            dragStartX = e.getSceneX() - node.getLayoutX();
            dragStartY = e.getSceneY() - node.getLayoutY();
            node.toFront();
        });

        node.setOnMouseDragged(e -> {
            if (connectMode) return;
            double newX = e.getSceneX() - dragStartX;
            double newY = e.getSceneY() - dragStartY;
            newX = Math.max(30, Math.min(newX, getWidth() - 30));
            newY = Math.max(30, Math.min(newY, getHeight() - 30));
            node.setPosition(newX, newY);
            updateAllConnections();
        });
    }

    private void setupClick(DeviceNodeView node) {
        node.setOnMouseClicked(e -> {
            if (connectMode) {
                handleConnectModeClick(node);
            } else {
                selectNode(node);
                if (onDeviceSelected != null) onDeviceSelected.accept(node.getViewModel());
            }
        });
    }

    private void selectNode(DeviceNodeView selected) {
        nodeViews.values().forEach(n -> n.setSelected(false));
        selected.setSelected(true);
    }

    // --- Modo de Conexão ---

    public void enterConnectMode() {
        connectMode        = true;
        connectModeSource  = null;
        connectPreviewLine.setVisible(false);
        setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #ffd54f; -fx-border-width: 2;");

        setOnMouseMoved(e -> {
            if (connectModeSource != null) {
                connectPreviewLine.setEndX(e.getX());
                connectPreviewLine.setEndY(e.getY());
            }
        });
    }

    public void exitConnectMode() {
        connectMode       = false;
        connectModeSource = null;
        connectPreviewLine.setVisible(false);
        setOnMouseMoved(null);
        setStyle("-fx-background-color: #1a1a2e;");
    }

    private void handleConnectModeClick(DeviceNodeView node) {
        if (connectModeSource == null) {
            connectModeSource = node;
            node.setSelected(true);
            connectPreviewLine.setStartX(node.getCenterX());
            connectPreviewLine.setStartY(node.getCenterY());
            connectPreviewLine.setEndX(node.getCenterX());
            connectPreviewLine.setEndY(node.getCenterY());
            connectPreviewLine.setVisible(true);
            if (onFirstDeviceSelected != null) onFirstDeviceSelected.run();
            if (onDeviceSelected != null) onDeviceSelected.accept(node.getViewModel());
        } else if (connectModeSource != node) {
            if (onConnectionRequest != null) {
                onConnectionRequest.accept(new String[]{
                        connectModeSource.getViewModel().getId(),
                        node.getViewModel().getId()
                });
            }
            connectModeSource.setSelected(false);
            exitConnectMode();
        }
    }

    // --- Animação de Pacotes ---

    public void animatePacket(List<NetworkDevice> path, Runnable onFinish) {
        animator.animate(path, connectionLines, delivered -> {
            if (onFinish != null) onFinish.run();
        });
    }

    private void updateAllConnections() {
        connectionLines.forEach(ConnectionLineView::update);
    }

    /** Reconstrói o canvas a partir do ViewModel (após import de topologia). */
    public void rebuild() {
        getChildren().clear();
        getChildren().add(connectPreviewLine);
        nodeViews.clear();
        lineViews.clear();
        connectionLines.clear();

        viewModel.getDevices().forEach(this::addDeviceNode);
        viewModel.getConnections().forEach(this::addConnectionLine);
    }

    // --- Callbacks ---

    public void setOnDeviceSelected(Consumer<DeviceViewModel> cb)    { this.onDeviceSelected = cb; }
    public void setOnConnectionRequest(Consumer<String[]> cb)        { this.onConnectionRequest = cb; }
    public void setOnFirstDeviceSelected(Runnable cb)                { this.onFirstDeviceSelected = cb; }
    public Map<String, DeviceNodeView> getNodeViews()               { return Collections.unmodifiableMap(nodeViews); }
    public boolean isConnectMode()                                  { return connectMode; }
}