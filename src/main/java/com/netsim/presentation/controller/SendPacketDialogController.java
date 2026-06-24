package com.netsim.presentation.controller;

import com.netsim.domain.entity.NetworkDevice;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Collection;

public class SendPacketDialogController {

    @FXML private ComboBox<String> cmbSource;
    @FXML private ComboBox<String> cmbDestination;
    @FXML private TextField        txtPayload;
    @FXML private Spinner<Integer> spnTtl;
    @FXML private Button           btnSend;
    @FXML private Button           btnCancel;

    private boolean confirmed = false;

    @FXML
    public void initialize() {
        spnTtl.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 255, 64));
        btnSend.setOnAction(e -> {
            if (validate()) {
                confirmed = true;
                close();
            }
        });
        btnCancel.setOnAction(e -> close());
    }

    public void setDevices(Collection<NetworkDevice> devices) {
        devices.stream()
                .map(d -> d.getIpAddress() + " (" + d.getName() + ")")
                .forEach(s -> {
                    cmbSource.getItems().add(s);
                    cmbDestination.getItems().add(s);
                });
        if (!cmbSource.getItems().isEmpty()) {
            cmbSource.getSelectionModel().selectFirst();
            cmbDestination.getSelectionModel().select(
                    cmbDestination.getItems().size() > 1 ? 1 : 0);
        }
    }

    private boolean validate() {
        if (cmbSource.getValue() == null || cmbDestination.getValue() == null) {
            new Alert(Alert.AlertType.ERROR, "Selecione origem e destino.", ButtonType.OK).showAndWait();
            return false;
        }
        if (cmbSource.getValue().equals(cmbDestination.getValue())) {
            new Alert(Alert.AlertType.ERROR, "Origem e destino não podem ser iguais.", ButtonType.OK).showAndWait();
            return false;
        }
        return true;
    }

    private String extractIp(String entry) {
        return entry.split(" ")[0];
    }

    private void close() { ((Stage) btnSend.getScene().getWindow()).close(); }

    public boolean isConfirmed()    { return confirmed; }
    public String getSourceIp()     { return extractIp(cmbSource.getValue()); }
    public String getDestinationIp(){ return extractIp(cmbDestination.getValue()); }
    public String getPayload()      { return txtPayload.getText(); }
    public int getTtl()             { return spnTtl.getValue(); }
}