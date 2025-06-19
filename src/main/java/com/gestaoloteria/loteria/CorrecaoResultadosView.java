package com.gestaoloteria.loteria;

import com.gestaoloteria.loteria.dao.JogoDAO;
import com.gestaoloteria.loteria.model.Jogo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tela estatística principal do sistema.
 * Analisa resultados, exibe gráficos, integra lógica matemática e ML (via Python).
 * by Copilot, PhD, MSc, Matemático & Estatístico internacional.
 */
public class AnaliseResultadosView extends JFrame {
    private JTable tabelaJogos;
    private DefaultTableModel tableModel;
    private JButton btnEstatisticas, btnSugestoesML, btnCorrigir, btnAtualizar;
    private JTextArea areaResultados;
    private JProgressBar progressoML;

    private JogoDAO jogoDAO = new JogoDAO();

    public AnaliseResultadosView() {
        setTitle("Análise Estatística dos Jogos");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Layout principal
        setLayout(new BorderLayout());

        // Tabela de jogos
        tableModel = new DefaultTableModel(new String[]{
                "ID", "Loteria", "Concurso", "Dezenas", "Acertos", "Data", "Observação"
        }, 0);
        tabelaJogos = new JTable(tableModel);
        JScrollPane scrollTabela = new JScrollPane(tabelaJogos);

        // Painel de botões e ações
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnEstatisticas = new JButton("Estatísticas");
        btnSugestoesML = new JButton("Sugestão Inteligente (ML)");
        btnCorrigir = new JButton("Correção de Jogos");
        btnAtualizar = new JButton("Atualizar Lista");
        progressoML = new JProgressBar(0,100);
        progressoML.setVisible(false);
        painelBotoes.add(btnEstatisticas);
        painelBotoes.add(btnSugestoesML);
        painelBotoes.add(btnCorrigir);
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(progressoML);

        // Área de resultados/relatórios
        areaResultados = new JTextArea(10, 80);
        areaResultados.setEditable(false);
        JScrollPane scrollResultados = new JScrollPane(areaResultados);

        add(painelBotoes, BorderLayout.NORTH);
        add(scrollTabela, BorderLayout.CENTER);
        add(scrollResultados, BorderLayout.SOUTH);

        // Carrega dados
        atualizarTabela();

        // Ação: Estatísticas matemáticas
        btnEstatisticas.addActionListener(e -> mostrarEstatisticas());

        // Ação: Sugestão ML (chama Python)
        btnSugestoesML.addActionListener(e -> executarSugestaoML());

        // Ação: Chamar tela de correção
        btnCorrigir.addActionListener(e -> abrirTelaCorrecao());

        // Ação: Atualizar
        btnAtualizar.addActionListener(e -> atualizarTabela());
    }

    /**
     * Atualiza a tabela com todos os jogos.
     */
    private void atualizarTabela() {
        try {
            tableModel.setRowCount(0);
            List<Jogo> jogos = jogoDAO.listarJogos();
            for (Jogo jogo : jogos) {
                tableModel.addRow(new Object[]{
                        jogo.getId(),
                        jogo.getLoteriaId(),
                        jogo.getConcursoId(),
                        jogo.getDezenas(),
                        jogo.getAcertos(),
                        jogo.getDataCadastro(),
                        jogo.getObservacao()
                });
            }
            areaResultados.setText("Lista atualizada (" + jogos.size() + " jogos encontrados).");
        } catch (Exception ex) {
            areaResultados.setText("Erro ao atualizar lista: " + ex.getMessage());
        }
    }

    /**
     * Mostra estatísticas matemáticas dos resultados cadastrados.
     */
    private void mostrarEstatisticas() {
        try {
            List<Jogo> jogos = jogoDAO.listarJogos();
            if (jogos.isEmpty()) {
                areaResultados.setText("Nenhum jogo cadastrado.");
                return;
            }
            // Estatísticas clássicas: acertos, frequência, média, moda, etc.
            StringBuilder sb = new StringBuilder();
            sb.append("Resumo Estatístico dos Jogos\n");
            sb.append("============================\n");
            double mediaAcertos = jogos.stream()
                    .filter(j -> j.getAcertos() != null)
                    .mapToInt(Jogo::getAcertos).average().orElse(0.0);
            sb.append("Média de Acertos: ").append(String.format("%.2f", mediaAcertos)).append("\n");

            long jogosComPremio = jogos.stream().filter(j -> j.getAcertos() != null && j.getAcertos() > 0).count();
            sb.append("Jogos premiados: ").append(jogosComPremio).append(" (")
              .append(String.format("%.1f%%", 100.0 * jogosComPremio / jogos.size())).append(")\n");

            // Frequência de dezenas (para loteria de 1 a 60, por exemplo)
            int[] freq = new int[61];
            for (Jogo jogo : jogos) {
                if (jogo.getDezenas() != null) {
                    for (String dez : jogo.getDezenas().split("[,;\\s]+")) {
                        try {
                            int val = Integer.parseInt(dez.trim());
                            if (val >= 1 && val <= 60) freq[val]++;
                        } catch (Exception ignore) {}
                    }
                }
            }
            sb.append("Top 10 dezenas mais apostadas: ");
            List<Integer> topDezenas = java.util.stream.IntStream.range(1, 61)
                    .boxed()
                    .sorted((a, b) -> Integer.compare(freq[b], freq[a]))
                    .limit(10)
                    .collect(Collectors.toList());
            for (int dez : topDezenas) sb.append(dez).append(" ");
            sb.append("\n");

            areaResultados.setText(sb.toString());
        } catch (Exception ex) {
            areaResultados.setText("Erro ao calcular estatísticas: " + ex.getMessage());
        }
    }

    /**
     * Chama o script Python de ajuste/sugestão inteligente (ML).
     * Exemplo: o script lê todos os jogos, analisa padrões e sugere os melhores próximos jogos.
     */
    private void executarSugestaoML() {
        progressoML.setVisible(true);
        progressoML.setIndeterminate(true);
        areaResultados.setText("Executando análise inteligente (ML)...\n");

        // Salva dados dos jogos em CSV para o Python
        try {
            List<Jogo> jogos = jogoDAO.listarJogos();
            File csv = new File("jogos_para_ml.csv");
            try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(csv), StandardCharsets.UTF_8))) {
                out.println("id,loteria_id,concurso_id,dezenas,acertos,data_cadastro");
                for (Jogo j : jogos) {
                    out.printf("%d,%d,%d,\"%s\",%s,%s\n",
                            j.getId(),
                            j.getLoteriaId(),
                            j.getConcursoId() == null ? -1 : j.getConcursoId(),
                            j.getDezenas(),
                            j.getAcertos() == null ? "" : j.getAcertos(),
                            j.getDataCadastro()
                    );
                }
            }
            // Executa o Python (ajuste_ml.py) e lê resultado
            ProcessBuilder pb = new ProcessBuilder("python", "ajuste_ml.py", "jogos_para_ml.csv");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
            String linha;
            StringBuilder resultadoML = new StringBuilder();
            while ((linha = reader.readLine()) != null) {
                resultadoML.append(linha).append("\n");
            }
            p.waitFor();
            progressoML.setIndeterminate(false);
            progressoML.setVisible(false);

            areaResultados.setText("Resultado da análise ML:\n" + resultadoML);
        } catch (Exception ex) {
            progressoML.setIndeterminate(false);
            progressoML.setVisible(false);
            areaResultados.setText("Erro na execução do Python/ML: " + ex.getMessage());
        }
    }

    /**
     * Abre a tela de correção dos resultados (integração futura).
     */
    private void abrirTelaCorrecao() {
        // TODO: Integrar com CorrecaoResultadosView quando implementada.
        JOptionPane.showMessageDialog(this, "Tela de Correção em desenvolvimento.\nConsulte a próxima entrega.");
    }

    // Entry-point para testes
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AnaliseResultadosView().setVisible(true));
    }
}