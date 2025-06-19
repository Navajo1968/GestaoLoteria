package com.gestaoloteria.loteria.view;

import javax.swing.*;
import java.awt.*;

/**
 * Tela para correção/conferência dos jogos após sorteio.
 * (Estrutura inicial para integração com análise e banco)
 */
public class CorrecaoResultadosView extends JFrame {
    public CorrecaoResultadosView() {
        setTitle("Correção dos Jogos");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // TODO: Implementar layout, lógica de conferência, integração com JogoDAO
        JLabel label = new JLabel("Tela de Correção dos Jogos (em construção)");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CorrecaoResultadosView().setVisible(true));
    }
}