package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Scoreboard.
 * Author: LEI-129851
 * Date: 2026-11-21
 * Cyclomatic Complexity:
 * - constructor (implicit): 1
 * - initDatabase() (private): 2
 * - saveGame(): 2
 * - showScoreboard(): 4
 * Notes:
 * - initDatabase() is private: covered through saveGame()/showScoreboard();
 *   initDatabase2 forces its catch by putting H2 in exclusive mode (new connections are rejected).
 * - The tests use the real database (./data/scoreboard): the JOGOS table is dropped before and after each test.
 */
public class ScoreboardTest {

    private static final String DB_URL = "jdbc:h2:./data/scoreboard";

    private PrintStream originalOut;
    private PrintStream originalErr;
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;

    @BeforeEach
    void setUp() throws SQLException {
        sql("DROP TABLE IF EXISTS JOGOS");
        originalOut = System.out;
        originalErr = System.err;
        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @AfterEach
    void tearDown() throws SQLException {
        System.setOut(originalOut);
        System.setErr(originalErr);
        out = null;
        err = null;
        sql("DROP TABLE IF EXISTS JOGOS");
    }

    // ---------- helpers ----------

    private void sql(String statement) throws SQLException {
        try (Connection c = DriverManager.getConnection(DB_URL, "sa", "");
             Statement s = c.createStatement()) {
            s.execute(statement);
        }
    }

    private int countGames() throws SQLException {
        try (Connection c = DriverManager.getConnection(DB_URL, "sa", "");
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM JOGOS")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Replaces JOGOS by a table with the wrong columns, so INSERT and SELECT fail. */
    private void breakTable() throws SQLException {
        sql("CREATE TABLE JOGOS (x INT)");
    }

    // ---------- constructor (CC = 1) ----------

    @Test
    @DisplayName("constructor: a classe pode ser instanciada")
    void constructor() {
        assertNotNull(new Scoreboard(), "Error: expected an instance but got null");
    }

    // ---------- initDatabase (CC = 2, private) ----------

    @Test
    @DisplayName("initDatabase: cria a tabela JOGOS quando ela não existe")
    void initDatabase1() throws SQLException {
        Scoreboard.showScoreboard(); // calls initDatabase() first
        assertEquals(0, countGames(), "Error: expected empty JOGOS table to exist");
    }

    @Test
    @Timeout(10)
    @DisplayName("initDatabase: base de dados em modo exclusivo rejeita a ligação e o erro é reportado")
    void initDatabase2() throws SQLException {
        try (Connection lock = DriverManager.getConnection(DB_URL, "sa", "");
             Statement s = lock.createStatement()) {
            s.execute("SET EXCLUSIVE 1");
            try {
                Scoreboard.saveGame(1);
            } finally {
                s.execute("SET EXCLUSIVE 0");
            }
        }
        assertTrue(err.toString().contains("Erro ao inicializar base de dados"),
                "Error: expected init error message but got: " + err);
    }

    // ---------- saveGame (CC = 2) ----------

    @Test
    @DisplayName("saveGame: grava um jogo na base de dados")
    void saveGame1() throws SQLException {
        Scoreboard.saveGame(7);
        assertAll(
                () -> assertEquals(1, countGames(), "Error: expected 1 saved game but got " + countGames()),
                () -> assertEquals("", err.toString(), "Error: expected no error but got " + err)
        );
    }

    @Test
    @DisplayName("saveGame: tabela com colunas erradas faz o INSERT falhar e o erro é reportado")
    void saveGame2() throws SQLException {
        breakTable();
        Scoreboard.saveGame(7);
        assertTrue(err.toString().contains("Erro ao guardar jogo no scoreboard"),
                "Error: expected save error message but got: " + err);
    }

    // ---------- showScoreboard (CC = 4) ----------

    @Test
    @DisplayName("showScoreboard: sem jogos mostra a mensagem 'Ainda não há jogos registados.'")
    void showScoreboard1() {
        Scoreboard.showScoreboard();
        assertTrue(out.toString().contains("Ainda não há jogos registados."),
                "Error: expected 'no games' message but got: " + out);
    }

    @Test
    @DisplayName("showScoreboard: com um jogo mostra a linha desse jogo")
    void showScoreboard2() {
        Scoreboard.saveGame(7);
        Scoreboard.showScoreboard();
        String s = out.toString();
        assertAll(
                () -> assertTrue(s.contains("Total de Rajadas: 7"), "Error: expected 'Total de Rajadas: 7'"),
                () -> assertFalse(s.contains("Ainda não há jogos registados."),
                        "Error: expected no 'no games' message")
        );
    }

    @Test
    @DisplayName("showScoreboard: vários jogos são mostrados do mais recente para o mais antigo")
    void showScoreboard3() {
        Scoreboard.saveGame(5);
        Scoreboard.saveGame(9);
        Scoreboard.showScoreboard();
        String s = out.toString();
        assertTrue(s.indexOf("Total de Rajadas: 9") < s.indexOf("Total de Rajadas: 5"),
                "Error: expected the newest game (9) before the oldest (5)");
    }

    @Test
    @DisplayName("showScoreboard: tabela com colunas erradas faz o SELECT falhar e o erro é reportado")
    void showScoreboard4() throws SQLException {
        breakTable();
        Scoreboard.showScoreboard();
        assertTrue(err.toString().contains("Erro ao ler scoreboard"),
                "Error: expected read error message but got: " + err);
    }
}