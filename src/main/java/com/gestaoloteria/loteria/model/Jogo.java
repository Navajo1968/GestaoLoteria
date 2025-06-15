package com.gestaoloteria.loteria.model;

import java.time.LocalDate;
import java.util.List;

public class Jogo {
    private Integer id;
    private Integer loteriaId;
    private Integer concursoId;
    private String numeros;

    // Construtor padrão
    public Jogo() {}

    // Construtor adicional (usado em JogoImportacaoUtil)
    public Jogo(Integer loteriaId, int numeroConcurso, LocalDate dataConcurso, List<Integer> numeros) {
        this.loteriaId = loteriaId;
        this.concursoId = numeroConcurso;
        this.numeros = numerosToString(numeros);
        // dataConcurso pode ser usado conforme necessidade do seu domínio
    }

    private String numerosToString(List<Integer> numeros) {
        if (numeros == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numeros.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(numeros.get(i));
        }
        return sb.toString();
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getLoteriaId() {
        return loteriaId;
    }
    public void setLoteriaId(Integer loteriaId) {
        this.loteriaId = loteriaId;
    }
    public Integer getConcursoId() {
        return concursoId;
    }
    public void setConcursoId(Integer concursoId) {
        this.concursoId = concursoId;
    }
    public String getNumeros() {
        return numeros;
    }
    public void setNumeros(String numeros) {
        this.numeros = numeros;
    }
}