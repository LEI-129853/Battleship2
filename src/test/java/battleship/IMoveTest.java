package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for interface IMove.
 * Author: LEI-129853
 * Date: 2026-11-21
 * Cyclomatic Complexity:
 * - readMove() (static): 2
 * - getSummary() (default): 1
 * Note: the abstract methods of IMove are implemented and tested in MoveTest.
 */
public class IMoveTest {

    /** Minimal implementation of IMove that does NOT override getSummary(), to test the default method. */
    private IMove move;

    @BeforeEach
    void setUp() {
        move = new IMove() {
            @Override public int getNumber() { return 1; }
            @Override public List<IPosition> getShots() { return List.of(); }
            @Override public List<IGame.ShotResult> getShotResults() { return List.of(); }
            @Override public String processEnemyFire(boolean verbose) { return ""; }
            @Override public String toString() { return "IMove de teste"; }
        };
    }

    @AfterEach
    void tearDown() {
        move = null;
    }

    // ---------- readMove (CC = 2) ----------

    @Test
    @DisplayName("readMove: 0 tiros não entra no ciclo e devolve jogada sem tiros")
    void readMove1() {
        Move m = IMove.readMove(4, new Scanner("0"));
        assertAll(
                () -> assertNotNull(m, "Error: expected a move but got null"),
                () -> assertEquals(4, m.getNumber(), "Error: expected move number 4 but got " + m.getNumber()),
                () -> assertTrue(m.getShots().isEmpty(),
                        "Error: expected no shots but got " + m.getShots().size()),
                () -> assertTrue(m.getShotResults().isEmpty(),
                        "Error: expected no shot results but got " + m.getShotResults().size())
        );
    }

    @Test
    @DisplayName("readMove: 2 tiros entram no ciclo e são lidos como (linha, coluna)")
    void readMove2() {
        Move m = IMove.readMove(7, new Scanner("2 1 1 3 4"));
        assertAll(
                () -> assertEquals(7, m.getNumber(), "Error: expected move number 7 but got " + m.getNumber()),
                () -> assertEquals(2, m.getShots().size(),
                        "Error: expected 2 shots but got " + m.getShots().size()),
                () -> assertEquals(new Position(1, 1), m.getShots().get(0),
                        "Error: expected first shot (1,1) but got " + m.getShots().get(0)),
                () -> assertEquals(new Position(3, 4), m.getShots().get(1),
                        "Error: expected second shot (3,4) but got " + m.getShots().get(1))
        );
    }

    // ---------- getSummary (CC = 1, default method) ----------

    @Test
    @DisplayName("getSummary: a implementação por omissão devolve uma string vazia")
    void getSummary() {
        assertEquals("", move.getSummary(),
                "Error: expected empty summary but got '" + move.getSummary() + "'");
    }
}