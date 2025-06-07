package com.gestaoloteria.loteria;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

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

        // Botão principal para Cadastros
        MenuButton cadastrosMenu = new MenuButton("Cadastros");
        MenuItem loteriasItem = new MenuItem("Loterias");
        loteriasItem.setOnAction(e -> Main.showLoteriaCadastro()); // Abre tela de cadastro de loterias
        cadastrosMenu.getItems().add(loteriasItem);

        // Outros botões do menu principal
        Button btnProcessos = createMenuButton("Processos");
        Button btnSair = createMenuButton("Sair");
        btnSair.setOnAction(e -> System.exit(0));

        vbox.getChildren().addAll(title, spacer, cadastrosMenu, btnProcessos, btnSair);
        root.getChildren().add(vbox);
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", 20));
        btn.setStyle("-fx-background-radius: 6; -fx-background-color: #395886; -fx-text-fill: white; -fx-font-weight: bold;");
        btn.setPrefWidth(220);
        btn.setPrefHeight(45);
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-radius: 6; -fx-background-color: #238636; -fx-text-fill: white; -fx-font-weight: bold;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-radius: 6; -fx-background-color: #395886; -fx-text-fill: white; -fx-font-weight: bold;"));
        return btn;
    }

    public StackPane getRoot() {
        return root;
    }
}