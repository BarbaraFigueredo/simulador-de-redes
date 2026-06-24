package com.netsim.presentation.controller;

import com.netsim.domain.valueobject.DeviceType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddDeviceDialogController {

    @FXML private Label     lblTitle;
    @FXML private TextField txtName;
    @FXML private TextField txtIp;
    @FXML private TextField txtExtra;
    @FXML private Label     lblExtra;
    @FXML private Button    btnOk;
    @FXML private Button    btnCancel;

    private DeviceType deviceType;
    private boolean    confirmed = false;

    @FXML
    public void initialize() {
        btnOk.setOnAction(e -> {
            if (validate()) {
                confirmed = true;
                close();
            }
        });
        btnCancel.setOnAction(e -> close());
    }

    public void setDeviceType(DeviceType type) {
        this.deviceType = type;
        lblTitle.setText("Adicionar " + type.name().toLowerCase().replace("_", " "));

        int deviceCount = 1;
        String defaultName = switch (type) {
            case COMPUTER -> "PC-0" + deviceCount;
            case ROUTER   -> "RT-0" + deviceCount;
            case SWITCH   -> "SW-0" + deviceCount;
        };
        txtName.setText(defaultName);
        txtIp.setText(suggestIp(type));

        if (type == DeviceType.COMPUTER) {
            lblExtra.setText("Sistema Operacional:");
            txtExtra.setText("Linux");
            lblExtra.setVisible(true);
            txtExtra.setVisible(true);
        } else {
            lblExtra.setVisible(false);
            txtExtra.setVisible(false);
        }
    }

    private String suggestIp(DeviceType type) {
        return switch (type) {
            case COMPUTER -> "192.168.1.10";
            case ROUTER   -> "10.0.0.1";
            case SWITCH   -> "192.168.1.254";
        };
    }

    private boolean validate() {
        if (txtName.getText().isBlank()) {
            showError("Nome não pode ser vazio.");
            return false;
        }
        if (!txtIp.getText().matches("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$")) {
            showError("Endereço IP inválido. Use o formato 192.168.1.1");
            return false;
        }
        return true;
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    private void close() {
        ((Stage) btnOk.getScene().getWindow()).close();
    }

    public boolean isConfirmed()   { return confirmed; }
    public String getDeviceName()  { return txtName.getText().trim(); }
    public String getIpAddress()   { return txtIp.getText().trim(); }
    public String getExtra()       { return txtExtra.isVisible() ? txtExtra.getText().trim() : null; }
}