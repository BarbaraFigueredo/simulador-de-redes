package com.netsim.presentation.view;

import com.netsim.domain.valueobject.DeviceType;
import com.netsim.presentation.viewmodel.DeviceViewModel;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Componente visual que representa um dispositivo de rede no canvas.
 *
 * Utiliza JavaFX Group para compor forma geométrica + ícone + labels.
 * Cada tipo de dispositivo tem um visual distinto:
 *   Computer → Retângulo azul
 *   Router   → Círculo vermelho
 *   Switch   → Losango verde
 */
public class DeviceNodeView extends Group {

    public static final double NODE_SIZE = 50.0;

    private final DeviceViewModel viewModel;
    private Shape                 shape;
    private Label                 nameLabel;
    private Label                 ipLabel;
    private boolean               selected = false;

    private static final Color COLOR_COMPUTER = Color.web("#4fc3f7");
    private static final Color COLOR_ROUTER   = Color.web("#ef5350");
    private static final Color COLOR_SWITCH   = Color.web("#66bb6a");
    private static final Color COLOR_INACTIVE = Color.web("#78909c");
    private static final Color COLOR_SELECTED = Color.web("#ffd54f");

    public DeviceNodeView(DeviceViewModel viewModel) {
        this.viewModel = viewModel;
        buildNode();
        setPosition(viewModel.getDevice().getPositionX(), viewModel.getDevice().getPositionY());
        addPulseAnimation();
    }

    private void buildNode() {
        DeviceType type = viewModel.getDevice().getType();
        Color color = getColorForType(type);

        shape = createShape(type, color);

        Label icon = new Label(getIconText(type));
        icon.setFont(Font.font("System", FontWeight.BOLD, 16));
        icon.setTextFill(Color.WHITE);
        icon.setAlignment(Pos.CENTER);
        icon.setLayoutX(-9);
        icon.setLayoutY(-10);

        nameLabel = new Label(viewModel.getDevice().getName());
        nameLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setLayoutX(-NODE_SIZE / 2);
        nameLabel.setLayoutY(NODE_SIZE / 2 + 4);

        ipLabel = new Label(viewModel.getDevice().getIpAddress());
        ipLabel.setFont(Font.font("Consolas", 10));
        ipLabel.setTextFill(Color.web("#b0bec5"));
        ipLabel.setLayoutX(-NODE_SIZE / 2);
        ipLabel.setLayoutY(NODE_SIZE / 2 + 18);

        DropShadow shadow = new DropShadow(10, Color.BLACK);
        shape.setEffect(shadow);

        getChildren().addAll(shape, icon, nameLabel, ipLabel);

        setOnMouseEntered(e -> shape.setOpacity(0.8));
        setOnMouseExited(e  -> shape.setOpacity(1.0));
    }

    private Shape createShape(DeviceType type, Color color) {
        return switch (type) {
            case COMPUTER -> {
                Rectangle r = new Rectangle(-NODE_SIZE / 2, -NODE_SIZE / 2, NODE_SIZE, NODE_SIZE);
                r.setArcWidth(8);
                r.setArcHeight(8);
                r.setFill(color);
                r.setStroke(color.darker());
                r.setStrokeWidth(2);
                yield r;
            }
            case ROUTER -> {
                Circle c = new Circle(NODE_SIZE / 2);
                c.setFill(color);
                c.setStroke(color.darker());
                c.setStrokeWidth(2);
                yield c;
            }
            case SWITCH -> {
                Polygon d = new Polygon(
                        0, -NODE_SIZE / 2,
                        NODE_SIZE / 2, 0,
                        0, NODE_SIZE / 2,
                        -NODE_SIZE / 2, 0
                );
                d.setFill(color);
                d.setStroke(color.darker());
                d.setStrokeWidth(2);
                yield d;
            }
        };
    }

    private Color getColorForType(DeviceType type) {
        if (!viewModel.getDevice().isActive()) return COLOR_INACTIVE;
        return switch (type) {
            case COMPUTER -> COLOR_COMPUTER;
            case ROUTER   -> COLOR_ROUTER;
            case SWITCH   -> COLOR_SWITCH;
        };
    }

    private String getIconText(DeviceType type) {
        return switch (type) {
            case COMPUTER -> "PC";
            case ROUTER   -> "RT";
            case SWITCH   -> "SW";
        };
    }

    private void addPulseAnimation() {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(1500), shape);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
    }

    public void setPosition(double x, double y) {
        setLayoutX(x);
        setLayoutY(y);
        viewModel.getDevice().setPositionX(x);
        viewModel.getDevice().setPositionY(y);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            shape.setStroke(COLOR_SELECTED);
            shape.setStrokeWidth(3);
        } else {
            Color c = getColorForType(viewModel.getDevice().getType());
            shape.setStroke(c.darker());
            shape.setStrokeWidth(2);
        }
    }

    public void flash(Color flashColor) {
        Color original = (Color) shape.getFill();
        FillTransition ft = new FillTransition(Duration.millis(300), shape, flashColor, original);
        ft.setCycleCount(2);
        ft.setAutoReverse(true);
        ft.play();
    }

    public void refreshLabel() {
        nameLabel.setText(viewModel.getDevice().getName());
        ipLabel.setText(viewModel.getDevice().getIpAddress());
        shape.setFill(getColorForType(viewModel.getDevice().getType()));
    }

    public double getCenterX() { return getLayoutX(); }
    public double getCenterY() { return getLayoutY(); }
    public DeviceViewModel getViewModel() { return viewModel; }
    public boolean isSelected() { return selected; }
}