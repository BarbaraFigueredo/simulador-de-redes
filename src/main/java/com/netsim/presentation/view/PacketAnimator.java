package com.netsim.presentation.view;

import com.netsim.domain.entity.NetworkDevice;
import javafx.animation.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Anima o percurso de um pacote ao longo do caminho de roteamento.
 *
 * Usa SequentialTransition + TranslateTransition para mover um círculo
 * colorido de dispositivo em dispositivo, representando o pacote.
 *
 * Também aciona flash nos dispositivos de cada hop (feedback visual).
 */
public class PacketAnimator {

    private static final double HOP_DURATION_MS = 600;
    private static final Color  PACKET_COLOR    = Color.web("#ffd54f");

    private final Pane                     canvas;
    private final Map<String, DeviceNodeView> nodeViews;

    public PacketAnimator(Pane canvas, Map<String, DeviceNodeView> nodeViews) {
        this.canvas    = canvas;
        this.nodeViews = nodeViews;
    }

    /**
     * Anima o pacote percorrendo a lista de dispositivos.
     *
     * @param path      Caminho calculado pelo Dijkstra
     * @param onFinish  Callback executado ao final da animação
     */
    public void animate(List<NetworkDevice> path,
                        List<ConnectionLineView> connectionLines,
                        Consumer<Boolean> onFinish) {
        if (path == null || path.size() < 2) {
            if (onFinish != null) onFinish.accept(false);
            return;
        }

        Circle packet = createPacketCircle(path.get(0));
        canvas.getChildren().add(packet);

        SequentialTransition sequence = new SequentialTransition();

        for (int i = 0; i < path.size() - 1; i++) {
            NetworkDevice from = path.get(i);
            NetworkDevice to   = path.get(i + 1);

            DeviceNodeView nodeFrom = nodeViews.get(from.getId());
            DeviceNodeView nodeTo   = nodeViews.get(to.getId());

            if (nodeFrom == null || nodeTo == null) continue;

            // Ativa visualmente o enlace durante a animação
            int hopIndex = i;
            connectionLines.stream()
                    .filter(l -> l.getConnection().connects(from, to))
                    .findFirst()
                    .ifPresent(line -> {
                        PauseTransition activate = new PauseTransition(Duration.ZERO);
                        activate.setOnFinished(e -> line.setActive(true));
                        sequence.getChildren().add(activate);
                    });

            // Move o pacote do nó origem ao nó destino
            TranslateTransition move = new TranslateTransition(
                    Duration.millis(HOP_DURATION_MS), packet);
            move.setToX(nodeTo.getCenterX());
            move.setToY(nodeTo.getCenterY());
            move.setInterpolator(Interpolator.EASE_BOTH);

            // Flash no dispositivo de destino ao chegar
            PauseTransition flashPause = new PauseTransition(Duration.millis(50));
            flashPause.setOnFinished(e -> {
                if (nodeTo != null) nodeTo.flash(PACKET_COLOR);
                // Desativa o enlace após usar
                connectionLines.stream()
                        .filter(l -> l.getConnection().connects(from, to))
                        .findFirst()
                        .ifPresent(line -> line.setActive(false));
            });

            sequence.getChildren().addAll(move, flashPause);
        }

        // Pulsa no destino e remove o círculo
        DeviceNodeView destNode = nodeViews.get(path.get(path.size() - 1).getId());
        if (destNode != null) {
            ScaleTransition arrive = new ScaleTransition(Duration.millis(300), packet);
            arrive.setToX(2.5);
            arrive.setToY(2.5);
            arrive.setAutoReverse(true);
            arrive.setCycleCount(2);
            sequence.getChildren().add(arrive);
        }

        sequence.setOnFinished(e -> {
            canvas.getChildren().remove(packet);
            if (onFinish != null) onFinish.accept(true);
        });

        // Posiciona o pacote na origem antes de animar
        DeviceNodeView originNode = nodeViews.get(path.get(0).getId());
        if (originNode != null) {
            packet.setTranslateX(originNode.getCenterX());
            packet.setTranslateY(originNode.getCenterY());
        }

        sequence.play();
    }

    private Circle createPacketCircle(NetworkDevice origin) {
        Circle c = new Circle(8, PACKET_COLOR);
        c.setStroke(Color.web("#ff8f00"));
        c.setStrokeWidth(2);

        DeviceNodeView originNode = nodeViews.get(origin.getId());
        if (originNode != null) {
            c.setTranslateX(originNode.getCenterX());
            c.setTranslateY(originNode.getCenterY());
        }

        // Efeito de brilho
        javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.8);
        c.setEffect(glow);

        return c;
    }
}