package battleship;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class DatabaseManager {

    // O nome do ficheiro da base de dados que será criado na pasta do projeto
    private static final String URL = "jdbc:sqlite:historico_batalha.db";

    // Método para criar a tabela no início do jogo
    public static void inicializarBaseDeDados() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            // Código SQL para criar a tabela
            String sql = "CREATE TABLE IF NOT EXISTS jogadas (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "linha INTEGER," +
                    "coluna INTEGER," +
                    "resultado TEXT," +
                    "data_hora DATETIME DEFAULT CURRENT_TIMESTAMP)";

            stmt.execute(sql);
            System.out.println("Base de dados inicializada com sucesso!");

        } catch (Exception e) {
            System.out.println("Erro ao inicializar Base de Dados: " + e.getMessage());
        }
    }

    // Método para guardar cada tiro
    public static void registarJogada(int linha, int coluna, String resultado) {
        String sql = "INSERT INTO jogadas(linha, coluna, resultado) VALUES(?,?,?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Substituir os '?' pelos valores reais
            pstmt.setInt(1, linha);
            pstmt.setInt(2, coluna);
            pstmt.setString(3, resultado);

            pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("Erro ao guardar jogada na BD: " + e.getMessage());
        }
    }
}