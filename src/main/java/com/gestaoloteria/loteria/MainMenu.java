package com.gestaoloteria.loteria;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class MainMenu {
    private StackPane root;

    public MainMenu() {
        root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #23395d 0%, #4069a3 100%);");

        VBox vbox = new VBox(30);
        vbox.setAlignment(Pos.CENTER);

        Label title = new Label("Gestão de Loterias");
        title.setFont(Font.font("Segoe UI", 38));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(8, Color.BLACK));
        title.setTextAlignment(TextAlignment.CENTER);

        Region spacer = new Region();
        spacer.setMinHeight(32);

        Button btnCadastros = createMenuButton("Cadastros");
        Button btnProcessos = createMenuButton("Processos");
        Button btnSair = createMenuButton("Sair");

        btnSair.setOnAction(e -> System.exit(0));

        vbox.getChildren().addAll(title, spacer, btnCadastros, btnProcessos, btnSair);

        root.getChildren().add(vbox);
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", 20));
        btn.setPrefWidth(220);
        btn.setStyle(
            "-fx-background-radius: 24px;" +
            "-fx-background-color: #ffffffcc;" +
            "-fx-text-fill: #23395d;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, #00000044, 4, 0, 0, 2);"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-radius: 24px;" +
            "-fx-background-color: #4069a3;" +
            "-fx-text-fill: #fff;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, #4069a3, 8, 0, 0, 4);"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-radius: 24px;" +
            "-fx-background-color: #ffffffcc;" +
            "-fx-text-fill: #23395d;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, #00000044, 4, 0, 0, 2);"
        ));
        return btn;
    }

    public StackPane getRoot() {
        return root;
    }
}