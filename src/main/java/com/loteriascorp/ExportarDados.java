package com.loteriascorp;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.experiment.InstanceQuery;

import java.io.File;

public class ExportarDados {

    public static void main(String[] args) {
        int idLoteria = 1; // Exemplo de idLoteria, pode ser parametrizado
        exportarDadosParaArff(idLoteria);
    }

    public static void exportarDadosParaArff(int idLoteria) {
        try {
            // Carregar dados do banco de dados
            InstanceQuery query = new InstanceQuery();
            query.setDatabaseURL("jdbc:postgresql://localhost:5432/gestaoloterias");
            query.setUsername("postgres");
            query.setPassword("@NaVaJo68#PostGre#");
            query.setQuery("SELECT * FROM tb_historico_jogos WHERE id_loterias = " + idLoteria);
            Instances data = query.retrieveInstances();

            // Salvar dados no formato ARFF
            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            saver.setFile(new File("path/to/historical/data_" + idLoteria + ".arff"));
            saver.writeBatch();
            
            System.out.println("Dados exportados com sucesso para o formato ARFF.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}