package com.loteriascorp.controller;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.loteriascorp.service.GeradorJogosOptimizado;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import java.util.stream.Collectors;

public class GerarProbabilidadeController {
    private static final Logger logger = LogManager.getLogger(GerarProbabilidadeController.class);
    
    @FXML private TextField txtQuantidadeJogos;
    @FXML private TextArea txtResultado;
    
    private final GeradorJogosOptimizado geradorJogos;
    private int idLoteria;
    
    public GerarProbabilidadeController() {
        this.geradorJogos = new GeradorJogosOptimizado();
    }
    
    public void setIdLoteria(int idLoteria) {
        this.idLoteria = idLoteria;
    }
    
    @FXML
    private void gerarProbabilidades() {
        try {
            int quantidadeJogos = Integer.parseInt(txtQuantidadeJogos.getText());
            List<List<Integer>> jogos = geradorJogos.gerarJogos(idLoteria, quantidadeJogos);
            
            // Validação adicional dos jogos gerados
            boolean todosValidos = true;
            for (List<Integer> jogo : jogos) {
                try {
                    geradorJogos.validarJogoAvancado(jogo);
                } catch (IllegalStateException e) {
                    logger.warn("Jogo inválido: {}", e.getMessage());
                    todosValidos = false;
                    break;
                }
            }
            
            if (todosValidos) {
                geradorJogos.salvarJogosGerados(idLoteria, jogos);
                exibirJogosGerados(jogos);
            } else {
                // Se algum jogo for inválido, gera novamente
                gerarProbabilidades();
            }
            
        } catch (NumberFormatException e) {
            logger.error("Quantidade de jogos inválida: ", e);
            // Mostrar mensagem de erro na interface
        } catch (Exception e) {
            logger.error("Erro ao gerar probabilidades: ", e);
            // Mostrar mensagem de erro na interface
        }
    }
    
    private void exibirJogosGerados(List<List<Integer>> jogos) {
        StringBuilder sb = new StringBuilder();
        sb.append("Jogos gerados:\n\n");
        
        for (int i = 0; i < jogos.size(); i++) {
            sb.append(String.format("Jogo %d: %s\n", 
                i + 1, 
                jogos.get(i).stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(" - "))
            ));
        }
        
        txtResultado.setText(sb.toString());
    }
}