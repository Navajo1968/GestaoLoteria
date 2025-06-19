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

    // PADRÃO: mostrar lista de loterias
    public static void showLoteriaListaView() {
        LoteriaListaView view = new LoteriaListaView();
        stagePrincipal.setScene(new Scene(view.getRoot(), 700, 500));
    }

    // PADRÃO: mostrar cadastro de loteria (novo ou edição)
    public static void showLoteriaCadastroView(LoteriaCadastroView view) {
        stagePrincipal.setScene(new Scene(view.getRoot(), 700, 500));
    }
    // Alternativamente, se você cria o LoteriaCadastroView dentro desse método:
    // public static void showLoteriaCadastroView() {
    //     LoteriaCadastroView view = new LoteriaCadastroView();
    //     stagePrincipal.setScene(new Scene(view.getRoot(), 700, 500));
    // }

    public static void showProcessosView() {
        ProcessosView view = new ProcessosView();
        stagePrincipal.setScene(new Scene(view.getRoot(), 800, 500));
    }

    public static void showGerarJogosView() {
        new GerarJogosView().show();
    }

    public static void showCorrecaoResultadosView() {
        new CorrecaoResultadosView().show();
    }
    
    public static Stage getStagePrincipal() {
        return stagePrincipal;
    }

    public static void main(String[] args) {
        launch(args);
    }

}