package com.gestaoloteria.loteria.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Jogo {
    private Integer id;
    private Integer loteriaId;
    private Integer concursoId;
    private Integer numeroConcursoPrevisto;
    private String numeros; // ex: "01,03,08,09,13,15,16,17,18,19,20,21,22,23,25"
    private LocalDateTime dataHora;
    private Integer acertos;
    private String observacao;

    public Jogo() {}

    public Jogo(Integer loteriaId, Integer concursoId, Integer numeroConcursoPrevisto, String numeros, LocalDateTime dataHora, Integer acertos, String observacao) {
        this.loteriaId = loteriaId;
        this.concursoId = concursoId;
        this.numeroConcursoPrevisto = numeroConcursoPrevisto;
        this.numeros = numeros;
        this.dataHora = dataHora;
        this.acertos = acertos;
        this.observacao = observacao;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getLoteriaId() { return loteriaId; }
    public void setLoteriaId(Integer loteriaId) { this.loteriaId = loteriaId; }

    public Integer getConcursoId() { return concursoId; }
    public void setConcursoId(Integer concursoId) { this.concursoId = concursoId; }

    public Integer getNumeroConcursoPrevisto() { return numeroConcursoPrevisto; }
    public void setNumeroConcursoPrevisto(Integer numeroConcursoPrevisto) { this.numeroConcursoPrevisto = numeroConcursoPrevisto; }

    public String getNumeros() { return numeros; }
    public void setNumeros(String numeros) { this.numeros = numeros; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public Integer getAcertos() { return acertos; }
    public void setAcertos(Integer acertos) { this.acertos = acertos; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    // NOVO MÉTODO CORRETO!
    public List<Integer> getNumerosList() {
        if (numeros == null || numeros.isEmpty()) return java.util.Collections.emptyList();
        return Arrays.stream(numeros.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}