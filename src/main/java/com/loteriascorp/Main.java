package com.loteriascorp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.loteriascorp.service.AnaliseConcursoScheduler;

public class Main extends Application {
    private AnaliseConcursoScheduler scheduler;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Iniciar o scheduler
        scheduler = new AnaliseConcursoScheduler();
        scheduler.iniciarAnaliseAutomatica();

        Parent root = FXMLLoader.load(getClass().getResource("/com/loteriascorp/view/Menu.fxml"));
        primaryStage.setTitle("Gestão de Loterias");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        // Garante que o scheduler seja encerrado quando a aplicação for fechada
        if (scheduler != null) {
            scheduler.shutdown();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}