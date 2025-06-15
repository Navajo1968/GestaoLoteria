package com.gestaoloteria.loteria.ui;

import com.gestaoloteria.loteria.model.Concurso;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ConcursoRow {

    private final SimpleIntegerProperty numeroConcurso;
    private final SimpleStringProperty dataConcurso;
    private final SimpleStringProperty dezenas;

    public ConcursoRow(Concurso concurso, String dezenas) {
        this.numeroConcurso = new SimpleIntegerProperty(concurso.getNumero());
        this.dataConcurso = new SimpleStringProperty(concurso.getData() != null ? concurso.getData().toString() : "");
        this.dezenas = new SimpleStringProperty(dezenas);
    }

    public SimpleIntegerProperty numeroConcursoProperty() { return numeroConcurso; }
    public SimpleStringProperty dataConcursoProperty() { return dataConcurso; }
    public SimpleStringProperty dezenasProperty() { return dezenas; }
}