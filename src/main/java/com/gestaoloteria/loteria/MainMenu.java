package com.gestaoloteria.loteria;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class MainMenu {

    public static Scene createMainMenuScene() {
        VBox root = new VBox(28);
        root.setPadding(new Insets(60));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50 0%, #2980b9 100%);");

        Button btnCadastros = createMenuButton("Cadastros");
        btnCadastros.setOnAction(e -> Main.showLoteriaListaView());

        Button btnProcessos = createMenuButton("Processos");
        btnProcessos.setOnAction(e -> Main.showProcessosView());

        Button btnGerarJogos = createMenuButton("Gerar Jogos");
        btnGerarJogos.setOnAction(e -> Main.showGerarJogosView());

        // NOVO BOTÃO: Correção Jogos
        Button btnCorrecaoJogos = createMenuButton("Correção Jogos");
        btnCorrecaoJogos.setOnAction(e -> Main.showCorrecaoResultadosView());

        // ===============================
        // ALTERAÇÃO em 22/06/2025:
        // Adicionado botão "Gerar Jogos Inteligentes" no menu principal,
        // para abrir futura tela de geração inteligente de jogos.
        // ===============================
        Button btnGerarJogosInteligentes = createMenuButton("Gerar Jogos Inteligentes");
        btnGerarJogosInteligentes.setOnAction(e -> Main.showGerarJogosInteligentesView());
        // ===============================

        Button btnSair = createMenuButton("Sair");
        btnSair.setOnAction(e -> System.exit(0));

        root.getChildren().addAll(
            btnCadastros,
            btnProcessos,
            btnGerarJogos,
            btnCorrecaoJogos,
            btnGerarJogosInteligentes, // novo botão aqui!
            btnSair
        );

        return new Scene(root, 510, 400);
    }

    private static Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", 24));
        btn.setPrefWidth(280);
        btn.setPrefHeight(64);
        btn.setStyle("-fx-background-radius: 22; -fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold;");
        btn.setEffect(new DropShadow(7, Color.DARKGRAY));
        return btn;
    }
}