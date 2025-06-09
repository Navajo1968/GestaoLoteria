package com.gestaoloteria.loteria;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage mainStage;

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

    public static void showLoteriaLista() {
        LoteriaListaView view = new LoteriaListaView();
        Scene scene = new Scene(view.getRoot(), 900, 500);
        mainStage.setScene(scene);
    }

    public static void showMainMenu() {
        MainMenu menu = new MainMenu();
        Scene scene = new Scene(menu.getRoot(), 500, 400);
        mainStage.setScene(scene);
    }

    public static void showLoteriaCadastro(LoteriaCadastroView form) {
        Scene scene = new Scene(form.getRoot(), 700, 500);
        mainStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}