package com.gestaoloteria.loteria.ui;

import com.gestaoloteria.loteria.model.Concurso;

import javafx.beans.property.*;
import java.time.format.DateTimeFormatter;

public class ConcursoRow {
    private final IntegerProperty numeroConcurso;
    private final StringProperty dataConcurso;
    private final StringProperty dezenas;

    public ConcursoRow(Concurso concurso, String dezenas) {
        this.numeroConcurso = new SimpleIntegerProperty(concurso.getNumeroConcurso());
        this.dataConcurso = new SimpleStringProperty(concurso.getDataConcurso().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        this.dezenas = new SimpleStringProperty(dezenas);
    }

    public IntegerProperty numeroConcursoProperty() { return numeroConcurso; }
    public StringProperty dataConcursoProperty() { return dataConcurso; }
    public StringProperty dezenasProperty() { return dezenas; }
}