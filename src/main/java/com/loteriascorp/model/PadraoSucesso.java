package com.loteriascorp.model;

public class PadraoSucesso {
    private final double proporcaoPares;
    private final int[] distribuicaoDezenas;
    private final int sequenciasConsecutivas;
    private final double somaTotal;
    
    public PadraoSucesso(double proporcaoPares, int[] distribuicaoDezenas, 
                        int sequenciasConsecutivas, double somaTotal) {
        this.proporcaoPares = proporcaoPares;
        this.distribuicaoDezenas = distribuicaoDezenas;
        this.sequenciasConsecutivas = sequenciasConsecutivas;
        this.somaTotal = somaTotal;
    }
    
    public double getProporçaoPares() {
        return proporcaoPares;
    }
    
    public int[] getDistribuicaoDezenas() {
        return distribuicaoDezenas.clone();
    }
    
    public int getSequenciasConsecutivas() {
        return sequenciasConsecutivas;
    }
    
    public double getSomaTotal() {
        return somaTotal;
    }
}