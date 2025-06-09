package com.gestaoloteria.loteria;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage stagePrincipal;

    @Override
    public void start(Stage primaryStage) {
        stagePrincipal = primaryStage;
        showMainMenu();
        primaryStage.setTitle("Gestão de Loteria");
        primaryStage.show();
    }

    public static void showMainMenu() {
        stagePrincipal.setScene(MainMenu.createMainMenuScene());
    }

    public static void showLoteriaListaView() {
        LoteriaListaView view = new LoteriaListaView();
        stagePrincipal.setScene(new Scene(view.getRoot(), 700, 500));
    }

    public static void showProcessosView() {
        ProcessosView view = new ProcessosView();
        stagePrincipal.setScene(new Scene(view.getRoot(), 800, 500));
    }

    public static void main(String[] args) {
        launch(args);
    }
}