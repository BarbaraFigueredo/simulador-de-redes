package com.netsim.presentation.controller;

import com.netsim.config.AppConfig;
import com.netsim.domain.entity.NetworkDevice;
import com.netsim.domain.valueobject.DeviceType;
import com.netsim.presentation.view.NetworkCanvas;
import com.netsim.presentation.viewmodel.DeviceViewModel;
import com.netsim.presentation.viewmodel.MainViewModel;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Controller principal — une a FXML com o MainViewModel.
 *
 * Responde a eventos da UI (cliques, menus), delega lógica aos
 * Use Cases via ViewModel, e atualiza o canvas conforme o estado muda.
 */
public class MainController {

    // --- Injeções FXML ---
    @FXML private BorderPane rootPane;
    @FXML private StackPane  canvasContainer;
    @FXML private VBox       deviceListContainer;
    @FXML private ListView<DeviceViewModel> deviceListView;
    @FXML private Label      lblDeviceName;
    @FXML private Label      lblDeviceIp;
    @FXML private Label      lblDeviceType;
    @FXML private Label      lblDeviceStatus;
    @FXML private Label      lblDeviceExtra;
    @FXML private ListView<String> routeListView;
    @FXML private ListView<String> logListView;
    @FXML private Label      lblStats;
    @FXML private ToggleButton btnStepMode;
    @FXML private Button     btnConnect;
    @FXML private Label      statusBar;

    private MainViewModel viewModel;
    private NetworkCanvas networkCanvas;
    private AppConfig     config;
    private boolean       connectModeActive = false;

    @FXML
    public void initialize() {
        config    = AppConfig.getInstance();
        viewModel = new MainViewModel(config);

        networkCanvas = new NetworkCanvas(viewModel);
        canvasContainer.getChildren().add(networkCanvas);
        networkCanvas.prefWidthProperty().bind(canvasContainer.widthProperty());
        networkCanvas.prefHeightProperty().bind(canvasContainer.heightProperty());

        bindUI();
        setupCallbacks();
        setStatus("Pronto. Adicione dispositivos e conecte-os.");
    }

    private void bindUI() {
        deviceListView.setItems(viewModel.getDevices());
        deviceListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    if (selected != null) showDeviceDetails(selected);
                });

        logListView.setItems(viewModel.getLogs());
        lblStats.textProperty().bind(viewModel.statsTextProperty());

        if (btnStepMode != null) {
            btnStepMode.selectedProperty().bindBidirectional(viewModel.stepModeProperty());
        }
    }

    private void setupCallbacks() {
        networkCanvas.setOnDeviceSelected(vm -> {
            viewModel.selectDevice(vm.getId());
            showDeviceDetails(vm);
            deviceListView.getSelectionModel().select(vm);
        });

        networkCanvas.setOnFirstDeviceSelected(() ->
                setStatus("Primeiro dispositivo selecionado. Clique no segundo para conectar."));

        networkCanvas.setOnConnectionRequest(ids -> {
            showConnectDialog(ids[0], ids[1]);
            setConnectMode(false);
        });
    }

    // --- Ações de Dispositivos ---

    @FXML
    public void onAddComputer() {
        showAddDeviceDialog(DeviceType.COMPUTER);
    }

    @FXML
    public void onAddRouter() {
        showAddDeviceDialog(DeviceType.ROUTER);
    }

    @FXML
    public void onAddSwitch() {
        showAddDeviceDialog(DeviceType.SWITCH);
    }

    @FXML
    public void onRemoveDevice() {
        DeviceViewModel selected = deviceListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selecione um dispositivo na lista para remover.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Remover " + selected.getDevice().getName() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar remoção");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                config.getRemoveDeviceUseCase().execute(selected.getId());
                networkCanvas.rebuild();
                clearDeviceDetails();
                setStatus("Dispositivo removido: " + selected.getDevice().getName());
            }
        });
    }

    // --- Modo de Conexão ---

    @FXML
    public void onConnectDevices() {
        if (!connectModeActive && config.getTopology().getDeviceCount() < 2) {
            showAlert("Adicione pelo menos 2 dispositivos antes de conectar.");
            return;
        }
        setConnectMode(!connectModeActive);
    }

    private void setConnectMode(boolean active) {
        connectModeActive = active;
        if (active) {
            networkCanvas.enterConnectMode();
            btnConnect.setText("Cancelar Conexão");
            setStatus("Clique no primeiro dispositivo, depois no segundo para conectar.");
        } else {
            networkCanvas.exitConnectMode();
            btnConnect.setText("Conectar Dispositivos");
            setStatus("Modo de conexão encerrado.");
        }
    }

    private void showConnectDialog(String idA, String idB) {
        NetworkDevice devA = config.getTopologyService().findById(idA).orElse(null);
        NetworkDevice devB = config.getTopologyService().findById(idB).orElse(null);
        if (devA == null || devB == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Configurar Enlace");
        dialog.setHeaderText(devA.getName() + " ↔ " + devB.getName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField latencyField    = new TextField("10");
        TextField bandwidthField  = new TextField("100");
        VBox form = new VBox(8,
                new Label("Latência (ms):"),  latencyField,
                new Label("Largura de banda (Mbps):"), bandwidthField);
        form.setStyle("-fx-padding: 12;");
        dialog.getDialogPane().setContent(form);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    int latency   = Integer.parseInt(latencyField.getText().trim());
                    int bandwidth = Integer.parseInt(bandwidthField.getText().trim());
                    config.getConnectDevicesUseCase().execute(idA, idB, latency, bandwidth);
                    setStatus("Enlace criado: " + devA.getName() + " ↔ " + devB.getName());
                } catch (NumberFormatException e) {
                    showAlert("Latência e largura de banda devem ser números inteiros.");
                } catch (Exception e) {
                    showAlert("Erro ao criar enlace: " + e.getMessage());
                }
            }
        });
    }

    // --- Envio de Pacotes ---

    @FXML
    public void onSendPacket() {
        if (config.getTopology().getDeviceCount() < 2) {
            showAlert("Adicione pelo menos 2 dispositivos para enviar pacotes.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/netsim/fxml/send-packet-dialog.fxml"));
            Parent root = loader.load();
            SendPacketDialogController ctrl = loader.getController();
            ctrl.setDevices(config.getTopologyService().getAllDevices());

            Stage stage = new Stage();
            stage.setTitle("Enviar Pacote");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (ctrl.isConfirmed()) {
                executePacketSend(ctrl.getSourceIp(), ctrl.getDestinationIp(),
                        ctrl.getPayload(), ctrl.getTtl());
            }
        } catch (IOException e) {
            showAlert("Erro ao abrir diálogo: " + e.getMessage());
        }
    }

    private void executePacketSend(String srcIp, String dstIp, String payload, int ttl) {
        setStatus("Simulando envio de pacote: " + srcIp + " → " + dstIp);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                config.getSendPacketUseCase().execute(srcIp, dstIp, payload, ttl,
                        path -> Platform.runLater(() ->
                                networkCanvas.animatePacket(path, () ->
                                        setStatus("Simulação concluída."))));
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // --- Export / Import ---

    @FXML
    public void onExportTopology() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Salvar Topologia");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON", "*.json"));
        chooser.setInitialFileName("topologia.json");
        File file = chooser.showSaveDialog(rootPane.getScene().getWindow());
        if (file != null) {
            try {
                config.getExportTopologyUseCase().execute(file);
                setStatus("Topologia exportada: " + file.getName());
            } catch (IOException e) {
                showAlert("Erro ao exportar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onImportTopology() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Carregar Topologia");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON", "*.json"));
        File file = chooser.showOpenDialog(rootPane.getScene().getWindow());
        if (file != null) {
            try {
                config.getImportTopologyUseCase().execute(file);
                viewModel.rebuildFromTopology();
                networkCanvas.rebuild();
                setStatus("Topologia carregada: " + file.getName());
            } catch (IOException e) {
                showAlert("Erro ao importar: " + e.getMessage());
            }
        }
    }

    // --- Limpar / Reset ---

    @FXML
    public void onClearTopology() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Limpar toda a topologia?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                config.getTopology().clear();
                viewModel.rebuildFromTopology();
                networkCanvas.rebuild();
                clearDeviceDetails();
                setStatus("Topologia limpa.");
            }
        });
    }

    @FXML
    public void onClearLogs() {
        viewModel.clearLogs();
    }

    @FXML
    public void onResetStats() {
        viewModel.resetStats();
    }

    // --- Diálogos ---

    private void showAddDeviceDialog(DeviceType type) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/netsim/fxml/add-device-dialog.fxml"));
            Parent root = loader.load();
            AddDeviceDialogController ctrl = loader.getController();
            ctrl.setDeviceType(type);

            Stage stage = new Stage();
            stage.setTitle("Adicionar " + type.name().toLowerCase());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (ctrl.isConfirmed()) {
                try {
                    NetworkDevice device = config.getAddDeviceUseCase().execute(
                            ctrl.getDeviceName(), ctrl.getIpAddress(), type, ctrl.getExtra());
                    setStatus("Adicionado: " + device.getName() + " (" + device.getIpAddress() + ")");
                } catch (Exception e) {
                    showAlert(e.getMessage());
                }
            }
        } catch (IOException e) {
            showAlert("Erro ao abrir diálogo: " + e.getMessage());
        }
    }

    // --- Detalhes do Dispositivo ---

    private void showDeviceDetails(DeviceViewModel vm) {
        vm.refresh();
        lblDeviceName.setText(vm.getDevice().getName());
        lblDeviceIp.setText(vm.getDevice().getIpAddress());
        lblDeviceType.setText(vm.getDevice().getType().name());
        lblDeviceStatus.setText(vm.getDevice().getStatus().name());
        lblDeviceExtra.setText(vm.extraProperty().get());
        routeListView.setItems(vm.getRoutes());
    }

    private void clearDeviceDetails() {
        lblDeviceName.setText("-");
        lblDeviceIp.setText("-");
        lblDeviceType.setText("-");
        lblDeviceStatus.setText("-");
        lblDeviceExtra.setText("-");
        routeListView.getItems().clear();
    }

    // --- Utilitários ---

    private void setStatus(String message) {
        if (statusBar != null) {
            Platform.runLater(() -> statusBar.setText(message));
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("NetSim");
        alert.showAndWait();
    }
}