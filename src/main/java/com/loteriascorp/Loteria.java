package com.loteriascorp;

import java.util.Objects;

public class Loteria {
    private int id;
    private String nome;
    private String contexto;
    private int qtNumeros;
    private String dtInclusao;
    private boolean seg;
    private boolean ter;
    private boolean qua;
    private boolean qui;
    private boolean sex;
    private boolean sab;
    private boolean dom;
    private String horarioSorteio;

    // Construtores
    public Loteria() {
        // Construtor padrão
    }

    public Loteria(String nome, int qtNumeros) {
        this.nome = nome;
        this.qtNumeros = qtNumeros;
    }

    public Loteria(int id, String nome, String contexto, int qtNumeros, String dtInclusao,
                  boolean seg, boolean ter, boolean qua, boolean qui, 
                  boolean sex, boolean sab, boolean dom, String horarioSorteio) {
        this.id = id;
        this.nome = nome;
        this.contexto = contexto;
        this.qtNumeros = qtNumeros;
        this.dtInclusao = dtInclusao;
        this.seg = seg;
        this.ter = ter;
        this.qua = qua;
        this.qui = qui;
        this.sex = sex;
        this.sab = sab;
        this.dom = dom;
        this.horarioSorteio = horarioSorteio;
    }

    // Getters e setters com validações
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID não pode ser negativo");
        }
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser vazio");
        }
        this.nome = nome.trim();
    }

    public String getContexto() {
        return contexto;
    }

    public void setContexto(String contexto) {
        this.contexto = contexto != null ? contexto.trim() : "";
    }

    public int getQtNumeros() {
        return qtNumeros;
    }

    public void setQtNumeros(int qtNumeros) {
        if (qtNumeros <= 0) {
            throw new IllegalArgumentException("Quantidade de números deve ser maior que zero");
        }
        this.qtNumeros = qtNumeros;
    }

    public String getDtInclusao() {
        return dtInclusao;
    }

    public void setDtInclusao(String dtInclusao) {
        this.dtInclusao = dtInclusao;
    }

    // Getters e setters para dias da semana
    public boolean isSeg() {
        return seg;
    }

    public void setSeg(boolean seg) {
        this.seg = seg;
    }

    public boolean isTer() {
        return ter;
    }

    public void setTer(boolean ter) {
        this.ter = ter;
    }

    public boolean isQua() {
        return qua;
    }

    public void setQua(boolean qua) {
        this.qua = qua;
    }

    public boolean isQui() {
        return qui;
    }

    public void setQui(boolean qui) {
        this.qui = qui;
    }

    public boolean isSex() {
        return sex;
    }

    public void setSex(boolean sex) {
        this.sex = sex;
    }

    public boolean isSab() {
        return sab;
    }

    public void setSab(boolean sab) {
        this.sab = sab;
    }

    public boolean isDom() {
        return dom;
    }

    public void setDom(boolean dom) {
        this.dom = dom;
    }

    public String getHorarioSorteio() {
        return horarioSorteio;
    }

    public void setHorarioSorteio(String horarioSorteio) {
        this.horarioSorteio = horarioSorteio;
    }

    // Método utilitário para verificar se há sorteio em um dia específico
    public boolean temSorteioNoDia(String dia) {
        return switch (dia.toLowerCase()) {
            case "seg" -> seg;
            case "ter" -> ter;
            case "qua" -> qua;
            case "qui" -> qui;
            case "sex" -> sex;
            case "sab" -> sab;
            case "dom" -> dom;
            default -> false;
        };
    }

    // ToString para representação textual da loteria
    @Override
    public String toString() {
        return "Loteria{" +
               "id=" + id +
               ", nome='" + nome + '\'' +
               ", qtNumeros=" + qtNumeros +
               ", horarioSorteio='" + horarioSorteio + '\'' +
               '}';
    }

    // Equals e HashCode para comparação de objetos
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loteria loteria = (Loteria) o;
        return id == loteria.id && 
               Objects.equals(nome, loteria.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome);
    }
}