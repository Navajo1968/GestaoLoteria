package com.gestaoloteria.loteria;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LoteriaRow {
    private final int id;
    private final StringProperty nome;
    private final StringProperty descricao;

    public LoteriaRow(int id, String nome, String descricao) {
        this.id = id;
        this.nome = new SimpleStringProperty(nome);
        this.descricao = new SimpleStringProperty(descricao);
    }
    public int getId() { return id; }
    public StringProperty nomeProperty() { return nome; }
    public StringProperty descricaoProperty() { return descricao; }
    public String getNome() { return nome.get(); }
    public String getDescricao() { return descricao.get(); }
}