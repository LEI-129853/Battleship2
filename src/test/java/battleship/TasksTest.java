package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Tasks.
 * Author: LEI-129820
 * Date: 2026-11-21
 * Cyclomatic Complexity:
 * - menu(): 23
 * - terminaJogo() (private): 1
 * - resumoUltimaJogada() (private): 2
 * - menuHelp(): 1
 * - buildFleet(): 4
 * - readShip(): 1
 * - readPosition(): 1
 * - readClassicPosition(): 8
 * Notes:
 * - terminaJogo() calls System.exit(0) and is not tested (it would stop the JUnit JVM);
 *   the same applies to the "simula" loop with a game, which only ends through terminaJogo().
 * - resumoUltimaJogada(): the "empty list" branch is unreachable (it is only called after a move).
 * - readClassicPosition(): the second "else if" is unreachable (if part1 is a letter and part2
 *   is a number, part1+part2 always matches the first regex).
 */
public class TasksTest {

	/** The 11 ships of data/exemploTabuleiro.txt (a valid fleet). */
	private static final String FROTA_VALIDA =
			"caravela 0 0 s\n" +
					"galeao 0 3 o\n" +
					"barca 0 9 n\n" +
					"fragata 8 6 e\n" +
					"nau 9 0 e\n" +
					"caravela 5 2 o\n" +
					"nau 3 7 n\n" +
					"barca 3 1 s\n" +
					"barca 3 5 e\n" +
					"caravela 7 0 e\n" +
					"barca 7 4 o\n";

	private InputStream originalIn;
	private PrintStream originalOut;
	private ByteArrayOutputStream out;

	@BeforeEach
	void setUp() {
		originalIn = System.in;
		originalOut = System.out;
		out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
	}

	@AfterEach
	void tearDown() {
		System.setIn(originalIn);
		System.setOut(originalOut);
		out = null;
	}

	/** Runs Tasks.menu() with the given commands as keyboard input and returns the console output. */
	private String runMenu(String commands) {
		System.setIn(new ByteArrayInputStream(commands.getBytes()));
		Tasks.menu();
		return out.toString();
	}

	// ---------- readPosition (CC = 1) ----------

	@Test
	@DisplayName("readPosition: lê linha e coluna numéricas")
	void readPosition() {
		Position p = Tasks.readPosition(new Scanner("3 4"));
		assertAll(
				() -> assertEquals(3, p.getRow(), "Error: expected row 3 but got " + p.getRow()),
				() -> assertEquals(4, p.getColumn(), "Error: expected column 4 but got " + p.getColumn())
		);
	}

	// ---------- readShip (CC = 1) ----------

	@Test
	@DisplayName("readShip: lê tipo, posição e rumo de um navio")
	void readShip() {
		Ship s = Tasks.readShip(new Scanner("barca 0 9 n"));
		assertAll(
				() -> assertNotNull(s, "Error: expected a ship but got null"),
				() -> assertEquals("Barca", s.getCategory(), "Error: expected Barca but got " + s.getCategory()),
				() -> assertEquals(Compass.NORTH, s.getBearing(), "Error: expected NORTH but got " + s.getBearing())
		);
	}

	// ---------- buildFleet (CC = 4) ----------

	@Test
	@DisplayName("buildFleet: 11 navios válidos são todos adicionados")
	void buildFleet1() {
		Fleet f = Tasks.buildFleet(new Scanner(FROTA_VALIDA));
		assertEquals(11, f.getShips().size(), "Error: expected 11 ships but got " + f.getShips().size());
	}

	@Test
	@DisplayName("buildFleet: navio desconhecido (readShip devolve null) é ignorado")
	void buildFleet2() {
		Fleet f = Tasks.buildFleet(new Scanner("xpto 0 0 n\n" + FROTA_VALIDA));
		assertEquals(11, f.getShips().size(),
				"Error: expected 11 ships (unknown one ignored) but got " + f.getShips().size());
	}

	@Test
	@DisplayName("buildFleet: navio que colide (addShip falha) é ignorado")
	void buildFleet3() {
		// the first ship is repeated, so the second copy collides with it
		Fleet f = Tasks.buildFleet(new Scanner("caravela 0 0 s\n" + FROTA_VALIDA));
		assertEquals(11, f.getShips().size(),
				"Error: expected 11 ships (colliding one ignored) but got " + f.getShips().size());
	}

	@Test
	@DisplayName("buildFleet: Scanner nulo dispara o assert")
	void buildFleet4() {
		assertThrows(AssertionError.class, () -> Tasks.buildFleet(null),
				"Error: expected AssertionError for a null scanner");
	}

	// ---------- readClassicPosition (CC = 8) ----------

	@Test
	@DisplayName("readClassicPosition: Scanner vazio lança IllegalArgumentException")
	void readClassicPosition1() {
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("")),
				"Error: expected IllegalArgumentException for empty input");
	}

	@Test
	@DisplayName("readClassicPosition: formato junto 'A3'")
	void readClassicPosition2() {
		IPosition p = Tasks.readClassicPosition(new Scanner("A3"));
		assertEquals(new Position('A', 3), p, "Error: expected A3 but got " + p);
	}

	@Test
	@DisplayName("readClassicPosition: formato separado 'A 3'")
	void readClassicPosition3() {
		IPosition p = Tasks.readClassicPosition(new Scanner("A 3"));
		assertEquals(new Position('A', 3), p, "Error: expected A3 but got " + p);
	}

	@Test
	@DisplayName("readClassicPosition: minúsculas 'b 7' são convertidas")
	void readClassicPosition4() {
		IPosition p = Tasks.readClassicPosition(new Scanner("b 7"));
		assertEquals(new Position('B', 7), p, "Error: expected B7 but got " + p);
	}

	@Test
	@DisplayName("readClassicPosition: só números '33' lança IllegalArgumentException")
	void readClassicPosition5() {
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("33")),
				"Error: expected IllegalArgumentException for '33'");
	}

	@Test
	@DisplayName("readClassicPosition: letra sem número 'A X' lança IllegalArgumentException")
	void readClassicPosition6() {
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("A X")),
				"Error: expected IllegalArgumentException for 'A X'");
	}

	@Test
	@DisplayName("readClassicPosition: duas letras + número 'AB 3' lança IllegalArgumentException")
	void readClassicPosition7() {
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("AB 3")),
				"Error: expected IllegalArgumentException for 'AB 3'");
	}

	@Test
	@DisplayName("readClassicPosition: número negativo 'A -3' lança IllegalArgumentException")
	void readClassicPosition8() {
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("A -3")),
				"Error: expected IllegalArgumentException for 'A -3'");
	}

	// ---------- menuHelp (CC = 1) ----------

	@Test
	@DisplayName("menuHelp: mostra a ajuda com todos os comandos")
	void menuHelp() {
		Tasks.menuHelp();
		String s = out.toString();
		assertAll(
				() -> assertTrue(s.contains("AJUDA DO MENU"), "Error: expected help title"),
				() -> assertTrue(s.contains("gerafrota"), "Error: expected command gerafrota"),
				() -> assertTrue(s.contains("scoreboard"), "Error: expected command scoreboard"),
				() -> assertTrue(s.contains("desisto"), "Error: expected command desisto")
		);
	}

	// ---------- menu (CC = 23) ----------

	@Test
	@DisplayName("menu: 'desisto' logo à primeira termina sem exportar PDF")
	void menu1() {
		String s = runMenu("desisto\n");
		assertAll(
				() -> assertTrue(s.contains("Bons ventos!"), "Error: expected goodbye message"),
				() -> assertTrue(s.contains("Tempo total de jogo"), "Error: expected total time"),
				() -> assertFalse(s.contains("PDF guardado"), "Error: expected no PDF export")
		);
	}

	@Test
	@DisplayName("menu: comando desconhecido mostra mensagem de erro")
	void menu2() {
		String s = runMenu("xpto\ndesisto\n");
		assertTrue(s.contains("Que comando é esse"), "Error: expected unknown command message");
	}

	@Test
	@DisplayName("menu: 'ajuda' volta a mostrar a ajuda")
	void menu3() {
		String s = runMenu("ajuda\ndesisto\n");
		int first = s.indexOf("AJUDA DO MENU");
		assertTrue(s.indexOf("AJUDA DO MENU", first + 1) > first,
				"Error: expected help to be shown twice (start + command)");
	}

	@Test
	@DisplayName("menu: sem jogo, estado/mapa/tiros/rajada/simula não fazem nada")
	void menu4() {
		String s = runMenu("estado\nmapa\ntiros\nrajada\nsimula\ndesisto\n");
		assertAll(
				() -> assertFalse(s.contains("Estado da Frota"), "Error: expected no fleet status without a fleet"),
				() -> assertFalse(s.contains("Tempo desta jogada"), "Error: expected no move without a game"),
				() -> assertTrue(s.contains("Bons ventos!"), "Error: expected goodbye message")
		);
	}

	@Test
	@DisplayName("menu: 'pdf' sem rajadas avisa que não há nada para exportar")
	void menu5() {
		String s = runMenu("pdf\ndesisto\n");
		assertTrue(s.contains("Ainda não há rajadas para exportar."),
				"Error: expected 'no bursts to export' message");
	}

	@Test
	@DisplayName("menu: 'scoreboard' mostra o histórico de jogos")
	void menu6() {
		String s = runMenu("scoreboard\ndesisto\n");
		assertTrue(s.contains("SCOREBOARD DOS JOGOS PASSADOS"), "Error: expected scoreboard title");
	}

	@Test
	@DisplayName("menu: 'gerafrota' cria jogo; estado/mapa/tiros funcionam com frota")
	void menu7() {
		String s = runMenu("gerafrota\nestado\nmapa\ntiros\ndesisto\n");
		assertAll(
				() -> assertTrue(s.contains("Estado da Frota"), "Error: expected fleet status"),
				() -> assertTrue(s.contains("Bons ventos!"), "Error: expected goodbye message")
		);
	}

	@Test
	@DisplayName("menu: rajada manual mede o tempo, entra no diário e é exportada no 'pdf' e ao desistir")
	void menu8() {
		String s = runMenu("gerafrota\nrajada A1 B2 C3\npdf\ndesisto\n");
		assertAll(
				() -> assertTrue(s.contains("Tempo desta jogada"), "Error: expected move time"),
				() -> assertTrue(s.contains("PDF guardado em"), "Error: expected PDF export")
		);
	}

	@Test
	@DisplayName("menu: 'lefrota' lê a frota do teclado")
	void menu9() {
		String s = runMenu("lefrota\n" + FROTA_VALIDA + "estado\ndesisto\n");
		assertTrue(s.contains("Estado da Frota"), "Error: expected fleet status after reading the fleet");
	}
}