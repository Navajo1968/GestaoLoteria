package com.loteriascorp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.loteriascorp.service.JogoService;
import org.springframework.beans.factory.annotation.Autowired;

import com.loteriascorp.controller.GerarProbabilidadeController;

public class MenuController {
	private static final Logger logger = LogManager.getLogger(GerarProbabilidadeController.class);
	
    @FXML
    private void handleLoterias(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/loteriascorp/view/Loteria.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Cadastro de Loterias");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoteriasPreco(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/loteriascorp/view/LoteriaPreco.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Cadastro de Loterias Preço");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoteriasProbabilidade(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/loteriascorp/view/LoteriaProbabilidade.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Cadastro de Loterias Probabilidade");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoteriasHistorico(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/loteriascorp/view/LoteriaHistorico.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Cadastro de Loterias Histórico");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCarregarHistorico(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/loteriascorp/view/CarregarHistorico.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Carregar Histórico");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGerarProbabilidade() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/loteriascorp/view/GerarProbabilidade.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Gerar Probabilidade");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Erro ao abrir tela de Gerar Probabilidade: ", e);
        }
    }

    @FXML
    private void handleVerJogosGerados() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/loteriascorp/view/VerJogosGerados.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ver Jogos Gerados");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEncerrar(ActionEvent event) {
        // Obtém a referência do Stage atual e chama o método close para encerrar a aplicação
        Stage stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        stage.close();
    }

    @Autowired
    private JogoService jogoService;

    public void rodarML() {
        int idLoteria = 1; // ID da loteria (exemplo)
        int quantidadeJogos = 10; // Quantidade de jogos a serem gerados e simulados
        jogoService.acionarSimulacaoApostas(idLoteria, quantidadeJogos);
    }
}