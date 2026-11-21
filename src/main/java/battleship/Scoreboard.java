package battleship;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gere o histórico de jogos na base de dados H2.
 */
public class Scoreboard {
    // A base de dados será guardada num ficheiro local na pasta data do projeto
    private static final String DB_URL = "jdbc:h2:./data/scoreboard";
    private static final String USER = "sa";
    private static final String PASS = "";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Garante que a tabela JOGOS existe antes de qualquer operação.
     */
    private static void initDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS JOGOS (" +
                "id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                "rajadas INTEGER, " +
                "data_jogo VARCHAR(255))";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println("Erro ao inicializar base de dados: " + e.getMessage());
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    /**
     * Grava o resultado de um jogo no fim da partida.
     *
     * @param rajadas O número total de rajadas efetuadas no jogo.
     */
    public static void saveGame(int rajadas) {
        initDatabase();
        String sql = "INSERT INTO JOGOS (rajadas, data_jogo) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, rajadas);
            pstmt.setString(2, LocalDateTime.now().format(DATE_FORMATTER));
            pstmt.executeUpdate();

        } catch (Exception e) {
            System.err.println("Erro ao guardar jogo no scoreboard: " + e.getMessage());
        }
    }

    /**
     * Lê e imprime o histórico de todos os jogos passados na consola.
     */
    public static void showScoreboard() {
        initDatabase();
        String sql = "SELECT rajadas, data_jogo FROM JOGOS ORDER BY id DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=== SCOREBOARD DOS JOGOS PASSADOS ===");
            boolean temJogos = false;

            while (rs.next()) {
                temJogos = true;
                System.out.println("Data: " + rs.getString("data_jogo") +
                        " | Total de Rajadas: " + rs.getInt("rajadas"));
            }

            if (!temJogos) {
                System.out.println("Ainda não há jogos registados.");
            }
            System.out.println("=====================================\n");

        } catch (Exception e) {
            System.err.println("Erro ao ler scoreboard: " + e.getMessage());
        }
    }
}