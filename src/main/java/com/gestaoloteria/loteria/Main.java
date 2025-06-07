package com.gestaoloteria.loteria;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        MainMenu menu = new MainMenu();
        Scene scene = new Scene(menu.getRoot(), 500, 400);

        primaryStage.setTitle("Gestão de Loterias");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}