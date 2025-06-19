package com.gestaoloteria.loteria.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class CorrecaoResultadosView {

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Correção dos Jogos");

        BorderPane pane = new BorderPane();
        Label label = new Label("Tela de Correção dos Jogos (em construção)");
        pane.setCenter(label);
        BorderPane.setAlignment(label, Pos.CENTER);

        Scene scene = new Scene(pane, 600, 400);
        stage.setScene(scene);
        stage.show();
    }
}