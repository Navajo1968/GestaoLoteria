package com.loteriascorp.service;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class LotoFacilService {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/gestaoloterias";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "@NaVaJo68#PostGre#";
    private static final String LOTTOFACIL_API_URL = "https://servicebus2.caixa.gov.br/portaldeloterias/api/lotofacil";

    public static void main(String[] args) {
        try {
            String jsonResponse = fetchResults();
            JSONObject result = new JSONObject(jsonResponse);

            if (!recordExists(result.getInt("numero"))) {
                insertResultsIntoDB(result);
                insertIntoHistoricoJogos(result.getInt("numero"));
                System.out.println("Base de dados atualizada com sucesso.");
            } else {
                System.out.println("Registro já existente. Ignorando...");
            }
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String fetchResults() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(LOTTOFACIL_API_URL);
        HttpResponse response = httpClient.execute(request);

        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        reader.close();
        httpClient.close();

        return result.toString();
    }

    private static boolean recordExists(int numeroConcurso) throws Exception {
        String query = "SELECT 1 FROM tb_lotofacil_resultados WHERE numero_concurso = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, numeroConcurso);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void insertResultsIntoDB(JSONObject result) throws Exception {
        String insertQuery = "INSERT INTO tb_lotofacil_resultados (" +
                "numero_concurso, data_concurso, data_proximo_concurso, acumulado, dezenas_sorteadas_ordem_sorteio, " +
                "lista_dezenas, exibir_detalhamento_por_cidade, indicador_concurso_especial, lista_municipio_uf_ganhadores, " +
                "lista_rateio_premio, local_sorteio, nome_municipio_uf_sorteio, nome_time_coracao_mes_sorte, " +
                "numero_concurso_anterior, numero_concurso_final_0_5, numero_concurso_proximo, numero_jogo, observacao, " +
                "tipo_jogo, tipo_publicacao, ultimo_concurso, valor_arrecadado, valor_acumulado_concurso_0_5, " +
                "valor_acumulado_concurso_especial, valor_acumulado_proximo_concurso, valor_estimado_proximo_concurso, " +
                "valor_saldo_reserva_garantidora, valor_total_premio_faixa_um, created_at" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            pstmt.setInt(1, result.getInt("numero"));
            pstmt.setDate(2, new java.sql.Date(new SimpleDateFormat("dd/MM/yyyy").parse(result.getString("dataApuracao")).getTime()));
            pstmt.setDate(3, new java.sql.Date(new SimpleDateFormat("dd/MM/yyyy").parse(result.optString("dataProximoConcurso", "1970-01-01")).getTime()));
            pstmt.setBoolean(4, result.getBoolean("acumulado"));

            // Arrays
            pstmt.setArray(5, conn.createArrayOf("VARCHAR", result.getJSONArray("dezenasSorteadasOrdemSorteio").toList().toArray()));
            pstmt.setArray(6, conn.createArrayOf("VARCHAR", result.getJSONArray("listaDezenas").toList().toArray()));

            pstmt.setBoolean(7, result.getBoolean("exibirDetalhamentoPorCidade"));
            pstmt.setInt(8, result.getInt("indicadorConcursoEspecial"));

            // JSONB columns
            pstmt.setObject(9, result.getJSONArray("listaMunicipioUFGanhadores").toString(), java.sql.Types.OTHER);
            pstmt.setObject(10, result.getJSONArray("listaRateioPremio").toString(), java.sql.Types.OTHER);

            pstmt.setString(11, result.getString("localSorteio"));
            pstmt.setString(12, result.getString("nomeMunicipioUFSorteio"));

            // Remover caracteres nulos do campo nomeTimeCoracaoMesSorte
            String nomeTimeCoracaoMesSorte = result.getString("nomeTimeCoracaoMesSorte").replaceAll("\u0000", "");
            pstmt.setString(13, nomeTimeCoracaoMesSorte);

            pstmt.setInt(14, result.getInt("numeroConcursoAnterior"));
            pstmt.setInt(15, result.getInt("numeroConcursoFinal_0_5"));
            pstmt.setInt(16, result.getInt("numeroConcursoProximo"));
            pstmt.setInt(17, result.getInt("numeroJogo"));
            pstmt.setString(18, result.optString("observacao", ""));

            pstmt.setString(19, result.getString("tipoJogo"));
            pstmt.setInt(20, result.getInt("tipoPublicacao"));
            pstmt.setBoolean(21, result.getBoolean("ultimoConcurso"));

            pstmt.setBigDecimal(22, result.getBigDecimal("valorArrecadado"));
            pstmt.setBigDecimal(23, result.optBigDecimal("valorAcumuladoConcurso_0_5", BigDecimal.ZERO));
            pstmt.setBigDecimal(24, result.optBigDecimal("valorAcumuladoConcursoEspecial", BigDecimal.ZERO));
            pstmt.setBigDecimal(25, result.optBigDecimal("valorAcumuladoProximoConcurso", BigDecimal.ZERO));
            pstmt.setBigDecimal(26, result.optBigDecimal("valorEstimadoProximoConcurso", BigDecimal.ZERO));
            pstmt.setBigDecimal(27, result.optBigDecimal("valorSaldoReservaGarantidora", BigDecimal.ZERO));
            pstmt.setBigDecimal(28, result.optBigDecimal("valorTotalPremioFaixaUm", BigDecimal.ZERO));
            pstmt.setTimestamp(29, new Timestamp(System.currentTimeMillis()));

            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // Código de erro para violação de chave primária em PostgreSQL
                System.err.println("Registro já existente em tb_lotofacil_resultados. Ignorando...");
            } else {
                throw e;
            }
        }
    }

    private static void insertIntoHistoricoJogos(int numeroConcurso) throws Exception {
        String insertQuery = "INSERT INTO tb_historico_jogos (" +
                "dt_jogo, num1, num2, num3, num4, num5, num6, num7, num8, num9, num10, num11, num12, num13, num14, num15, id_loterias, num_concurso" +
                ") SELECT " +
                "data_concurso AS dt_jogo, " +
                "CAST(lista_dezenas[1] AS INTEGER) AS num1, " +
                "CAST(lista_dezenas[2] AS INTEGER) AS num2, " +
                "CAST(lista_dezenas[3] AS INTEGER) AS num3, " +
                "CAST(lista_dezenas[4] AS INTEGER) AS num4, " +
                "CAST(lista_dezenas[5] AS INTEGER) AS num5, " +
                "CAST(lista_dezenas[6] AS INTEGER) AS num6, " +
                "CAST(lista_dezenas[7] AS INTEGER) AS num7, " +
                "CAST(lista_dezenas[8] AS INTEGER) AS num8, " +
                "CAST(lista_dezenas[9] AS INTEGER) AS num9, " +
                "CAST(lista_dezenas[10] AS INTEGER) AS num10, " +
                "CAST(lista_dezenas[11] AS INTEGER) AS num11, " +
                "CAST(lista_dezenas[12] AS INTEGER) AS num12, " +
                "CAST(lista_dezenas[13] AS INTEGER) AS num13, " +
                "CAST(lista_dezenas[14] AS INTEGER) AS num14, " +
                "CAST(lista_dezenas[15] AS INTEGER) AS num15, " +
                "1 AS id_loterias, " +
                "numero_concurso AS num_concurso " +
                "FROM tb_lotofacil_resultados WHERE numero_concurso = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            pstmt.setInt(1, numeroConcurso);
            try {
                pstmt.executeUpdate();
            } catch (SQLException e) {
                if (e.getSQLState().equals("23505")) { // Código de erro para violação de chave primária em PostgreSQL
                    System.err.println("Registro já existente em tb_historico_jogos. Ignorando...");
                } else {
                    throw e;
                }
            }
        }
    }
}