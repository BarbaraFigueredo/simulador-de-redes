package com.netsim.presentation.view;

import com.netsim.domain.entity.Connection;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

/**
 * Componente visual que representa um enlace entre dois dispositivos.
 *
 * Atualiza dinamicamente suas coordenadas conforme os DeviceNodeViews
 * são arrastados no canvas.
 */
public class ConnectionLineView extends Group {

    private final Connection      connection;
    private final DeviceNodeView  nodeA;
    private final DeviceNodeView  nodeB;
    private final Line            line;
    private final Label           latencyLabel;

    private static final Color COLOR_DEFAULT  = Color.web("#546e7a");
    private static final Color COLOR_ACTIVE   = Color.web("#00e676");

    public ConnectionLineView(Connection connection, DeviceNodeView nodeA, DeviceNodeView nodeB) {
        this.connection = connection;
        this.nodeA      = nodeA;
        this.nodeB      = nodeB;

        line = new Line();
        line.setStroke(COLOR_DEFAULT);
        line.setStrokeWidth(2.5);
        line.getStrokeDashArray().addAll(0.0);

        latencyLabel = new Label(connection.getLatencyMs() + "ms");
        latencyLabel.setFont(Font.font("Consolas", 10));
        latencyLabel.setTextFill(Color.web("#90a4ae"));

        getChildren().addAll(line, latencyLabel);

        setMouseTransparent(true);
        update();
    }

    /** Atualiza as coordenadas da linha para acompanhar os nós. */
    public void update() {
        double x1 = nodeA.getCenterX();
        double y1 = nodeA.getCenterY();
        double x2 = nodeB.getCenterX();
        double y2 = nodeB.getCenterY();

        line.setStartX(x1);
        line.setStartY(y1);
        line.setEndX(x2);
        line.setEndY(y2);

        // Posiciona o label no meio do enlace
        latencyLabel.setLayoutX((x1 + x2) / 2 + 5);
        latencyLabel.setLayoutY((y1 + y2) / 2 - 10);
    }

    public void setActive(boolean active) {
        line.setStroke(active ? COLOR_ACTIVE : COLOR_DEFAULT);
        line.setStrokeWidth(active ? 3.5 : 2.5);
    }

    public Connection getConnection() { return connection; }
    public DeviceNodeView getNodeA()  { return nodeA; }
    public DeviceNodeView getNodeB()  { return nodeB; }
}