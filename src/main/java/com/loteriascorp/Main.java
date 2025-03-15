package com.loteriascorp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
//import com.loteriascorp.service.AnaliseConcursoScheduler;
import com.loteriascorp.service.JogoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Main extends Application {
    //private AnaliseConcursoScheduler scheduler;
    private static String[] savedArgs;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Iniciar o scheduler
        //scheduler = new AnaliseConcursoScheduler();
        //scheduler.iniciarAnaliseAutomatica();

        Parent root = FXMLLoader.load(getClass().getResource("/com/loteriascorp/view/Menu.fxml"));
        primaryStage.setTitle("Gestão de Loterias");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        // Garante que o scheduler seja encerrado quando a aplicação for fechada
        //if (scheduler != null) {
        //    scheduler.shutdown();
        //}
        super.stop();
    }

    public static void main(String[] args) {
        savedArgs = args;
        launch(args);
    }

    @Bean
    CommandLineRunner run(ApplicationContext ctx) {
        return args -> {
            JogoService jogoService = ctx.getBean(JogoService.class);

            // Acionar manualmente a simulação de apostas
            int idLoteria = 1; // ID da loteria (exemplo)
            int quantidadeJogos = 10; // Quantidade de jogos a serem gerados e simulados
            jogoService.acionarSimulacaoApostas(idLoteria, quantidadeJogos);
        };
    }

    @Override
    public void init() throws Exception {
        super.init();
        SpringApplication.run(Main.class, savedArgs);
    }
}