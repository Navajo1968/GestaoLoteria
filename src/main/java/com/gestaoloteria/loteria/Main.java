package com.gestaoloteria.loteria;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage mainStage; // <---- Adicione isso!

    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        MainMenu menu = new MainMenu();
        Scene scene = new Scene(menu.getRoot(), 500, 400);

        primaryStage.setTitle("Gestão de Loterias");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void showLoteriaCadastro() {
        LoteriaCadastroView view = new LoteriaCadastroView();
        Scene scene = new Scene(view.getRoot(), 700, 480);
        mainStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}