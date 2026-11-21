package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Game.
 * Author: LEI-129851
 * Date: 2026-11-21
 * Cyclomatic Complexity:
 * - printBoard(): 5 (+2 asserts)
 * - printShips() (private): 5 - covered through printBoard()
 * - printBoardShots() (private): 7 - covered through printBoard()
 * - printBoardFrame() (private): 6 - covered through printBoard()
 * - jsonShots(): 3 (+1 assert)
 * - constructor: 1
 * - getMyFleet(): 1
 * - getAlienFleet(): 1
 * - getMyMoves(): 1
 * - getAlienMoves(): 1
 * - randomEnemyFire(): 10
 * - readEnemyFire(): 6 (+1 assert)
 * - fireShots(): 3 (+1 assert)
 * - fireSingleShot(): 6 (+1 assert)
 * - getRepeatedShots(): 1
 * - getInvalidShots(): 1
 * - getHits(): 1
 * - getSunkShips(): 1
 * - getRemainingShips(): 1
 * - repeatedShot(): 3 (+1 assert)
 * - printMyBoard(): 1
 * - printAlienBoard(): 1
 * - over(): 1
 * Notes:
 * - jsonShots(): the catch of JsonProcessingException is unreachable (a list of plain maps always serializes).
 * - randomEnemyFire(): "if (!shots.contains(newShot))" can be false only by chance (random generator),
 *   so that branch may or may not be covered in a given run.
 */
public class GameTest {

	/** My fleet: a Barge at A1 (0,0) and a Caravel NORTH at F6 (occupies (5,5) and (6,5)). */
	private Fleet fleet;
	private Barge barge;
	private Caravel caravel;
	private Game game;

	private PrintStream originalOut;
	private ByteArrayOutputStream out;

	@BeforeEach
	void setUp() {
		fleet = new Fleet();
		barge = new Barge(Compass.NORTH, new Position(0, 0));
		caravel = new Caravel(Compass.NORTH, new Position(5, 5));
		fleet.addShip(barge);
		fleet.addShip(caravel);
		game = new Game(fleet);

		originalOut = System.out;
		out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
	}

	@AfterEach
	void tearDown() {
		System.setOut(originalOut);
		game = null;
		fleet = null;
		barge = null;
		caravel = null;
		out = null;
	}

	// ---------- helpers ----------

	private static List<IPosition> shots(IPosition... p) {
		return new ArrayList<>(List.of(p));
	}

	private static IMove moveWith(IPosition... p) {
		return new Move(1, shots(p), new ArrayList<>());
	}

	/** Returns the marker printed at (row, col) in the last board printed to the console. */
	private char cell(int row, int col) {
		String label = " " + (char) ('A' + row) + " |";
		String board = out.toString();
		int start = board.lastIndexOf(label);
		String line = board.substring(start, board.indexOf('\n', start));
		return line.charAt(label.length() + 1 + 2 * col);
	}

	/** Fires all board positions with index [from, to) in bursts of 3 (the last burst padded with the last position). */
	private static void fireBoard(Game g, int from, int to) {
		List<IPosition> burst = new ArrayList<>();
		for (int i = from; i < to; i++) {
			burst.add(new Position(i / Game.BOARD_SIZE, i % Game.BOARD_SIZE));
			if (burst.size() == Game.NUMBER_SHOTS) {
				g.fireShots(burst);
				burst = new ArrayList<>();
			}
		}
		if (!burst.isEmpty()) {
			while (burst.size() < Game.NUMBER_SHOTS)
				burst.add(burst.get(burst.size() - 1));
			g.fireShots(burst);
		}
	}

	// ---------- printBoard (CC = 5, +2 asserts) and its private helpers ----------

	@Test
	@DisplayName("printBoard: frota ou lista de jogadas nulas disparam os asserts")
	void printBoard1() {
		assertAll(
				() -> assertThrows(AssertionError.class, () -> Game.printBoard(null, new ArrayList<>(), false, false),
						"Error: expected AssertionError for null fleet"),
				() -> assertThrows(AssertionError.class, () -> Game.printBoard(fleet, null, false, false),
						"Error: expected AssertionError for null moves")
		);
	}

	@Test
	@DisplayName("printBoard: sem tiros nem legenda mostra só os navios ('#') e a água ('.')")
	void printBoard2() {
		Game.printBoard(fleet, List.of(moveWith(new Position(9, 9))), false, false);
		assertAll(
				() -> assertEquals('#', cell(0, 0), "Error: expected '#' at A1"),
				() -> assertEquals('#', cell(6, 5), "Error: expected '#' at G6"),
				() -> assertEquals('.', cell(9, 9), "Error: expected '.' at J10 (shots hidden)"),
				() -> assertFalse(out.toString().contains("LEGENDA"), "Error: expected no legend")
		);
	}

	@Test
	@DisplayName("printBoard: com legenda mostra o texto da legenda")
	void printBoard3() {
		Game.printBoard(fleet, new ArrayList<>(), false, true);
		assertTrue(out.toString().contains("LEGENDA"), "Error: expected the legend");
	}

	@Test
	@DisplayName("printBoard: tiros no navio ('*'), na água ('o') e fora do tabuleiro (ignorado)")
	void printBoard4() {
		List<IMove> moves = List.of(moveWith(new Position(5, 5), new Position(9, 9), new Position(-1, 0)));
		Game.printBoard(fleet, moves, true, false);
		assertAll(
				() -> assertEquals('*', cell(5, 5), "Error: expected '*' at F6 (shot on ship)"),
				() -> assertEquals('o', cell(9, 9), "Error: expected 'o' at J10 (shot on water)"),
				() -> assertEquals('#', cell(6, 5), "Error: expected '#' at G6 (not shot)")
		);
	}

	@Test
	@DisplayName("printBoard: navio afundado marca adjacentes ('-'); tiro num adjacente fica 'o'; tiro repetido não muda")
	void printBoard5() {
		barge.sink();
		List<IMove> moves = List.of(
				moveWith(new Position(1, 1), new Position(0, 0)),
				moveWith(new Position(0, 0)));
		Game.printBoard(fleet, moves, true, false);
		assertAll(
				() -> assertEquals('-', cell(0, 1), "Error: expected '-' at A2 (adjacent to sunk barge)"),
				() -> assertEquals('o', cell(1, 1), "Error: expected 'o' at B2 (shot on an adjacent position)"),
				() -> assertEquals('*', cell(0, 0), "Error: expected '*' at A1 (shot twice on the barge)")
		);
	}

	@Test
	@DisplayName("printBoard: moldura com colunas 1 a 10 e linhas A a J")
	void printBoard6() {
		Game.printBoard(new Fleet(), new ArrayList<>(), false, false);
		String s = out.toString();
		assertAll(
				() -> assertTrue(s.contains(" 1 2 3 4 5 6 7 8 9 10"), "Error: expected column header 1..10"),
				() -> assertTrue(s.contains(" A |"), "Error: expected row A"),
				() -> assertTrue(s.contains(" J |"), "Error: expected row J"),
				() -> assertEquals('.', cell(4, 4), "Error: expected '.' on an empty board")
		);
	}

	// ---------- jsonShots (CC = 3, +1 assert) ----------

	@Test
	@DisplayName("jsonShots: lista nula dispara o assert")
	void jsonShots1() {
		assertThrows(AssertionError.class, () -> Game.jsonShots(null), "Error: expected AssertionError for null");
	}

	@Test
	@DisplayName("jsonShots: lista vazia não entra no ciclo e dá um array vazio")
	void jsonShots2() {
		assertEquals("[ ]", Game.jsonShots(new ArrayList<>()), "Error: expected '[ ]'");
	}

	@Test
	@DisplayName("jsonShots: cada tiro aparece com a linha (letra) e a coluna (número)")
	void jsonShots3() {
		String json = Game.jsonShots(shots(new Position(0, 0), new Position(2, 4)));
		assertAll(
				() -> assertTrue(json.contains("\"row\" : \"A\""), "Error: expected row A in " + json),
				() -> assertTrue(json.contains("\"column\" : 1"), "Error: expected column 1 in " + json),
				() -> assertTrue(json.contains("\"row\" : \"C\""), "Error: expected row C in " + json),
				() -> assertTrue(json.contains("\"column\" : 5"), "Error: expected column 5 in " + json)
		);
	}

	// ---------- constructor and getters (CC = 1) ----------

	@Test
	@DisplayName("constructor: jogo novo sem jogadas e com contadores a zero")
	void constructor() {
		assertAll(
				() -> assertSame(fleet, game.getMyFleet(), "Error: expected my fleet"),
				() -> assertTrue(game.getAlienFleet().getShips().isEmpty(), "Error: expected an empty alien fleet"),
				() -> assertTrue(game.getMyMoves().isEmpty(), "Error: expected no moves of mine"),
				() -> assertTrue(game.getAlienMoves().isEmpty(), "Error: expected no alien moves"),
				() -> assertEquals(0, game.getHits(), "Error: expected 0 hits"),
				() -> assertEquals(0, game.getSunkShips(), "Error: expected 0 sunk ships"),
				() -> assertEquals(0, game.getInvalidShots(), "Error: expected 0 invalid shots"),
				() -> assertEquals(0, game.getRepeatedShots(), "Error: expected 0 repeated shots")
		);
	}

	@Test
	@DisplayName("getMyFleet: devolve a frota passada ao construtor")
	void getMyFleet() {
		assertSame(fleet, game.getMyFleet(), "Error: expected the fleet given to the constructor");
	}

	@Test
	@DisplayName("getAlienFleet: devolve uma frota (vazia)")
	void getAlienFleet() {
		assertNotNull(game.getAlienFleet(), "Error: expected an alien fleet, not null");
	}

	@Test
	@DisplayName("getMyMoves: lista de jogadas próprias começa vazia")
	void getMyMoves() {
		assertTrue(game.getMyMoves().isEmpty(), "Error: expected an empty list");
	}

	@Test
	@DisplayName("getAlienMoves: cada rajada recebida fica registada")
	void getAlienMoves() {
		game.fireShots(shots(new Position(9, 9), new Position(9, 8), new Position(9, 7)));
		assertEquals(1, game.getAlienMoves().size(), "Error: expected 1 alien move");
	}

	// ---------- randomEnemyFire (CC = 10) ----------

	@Test
	@DisplayName("randomEnemyFire: com muitas posições livres dispara 3 tiros diferentes")
	void randomEnemyFire1() {
		String json = game.randomEnemyFire();
		IMove m = game.getAlienMoves().get(0);
		assertAll(
				() -> assertEquals(1, game.getAlienMoves().size(), "Error: expected 1 alien move"),
				() -> assertEquals(3, m.getShots().stream().distinct().count(), "Error: expected 3 different shots"),
				() -> assertTrue(json.contains("\"row\""), "Error: expected the shots as JSON")
		);
	}

	@Test
	@DisplayName("randomEnemyFire: nunca dispara para posições já usadas nem adjacentes a navios afundados")
	void randomEnemyFire2() {
		barge.sink();
		List<IPosition> forbidden = barge.getAdjacentPositions();
		for (int i = 0; i < 20; i++)
			game.randomEnemyFire();
		List<IPosition> all = new ArrayList<>();
		game.getAlienMoves().forEach(m -> all.addAll(m.getShots()));
		assertAll(
				() -> assertEquals(all.size(), all.stream().distinct().count(), "Error: expected no repeated shots"),
				() -> assertTrue(all.stream().noneMatch(forbidden::contains),
						"Error: expected no shots adjacent to the sunk barge")
		);
	}

	@Test
	@DisplayName("randomEnemyFire: com menos de 3 posições livres repete a última posição")
	void randomEnemyFire3() {
		Game g = new Game(new Fleet());
		fireBoard(g, 0, 99); // only J10 (9,9) is left
		g.randomEnemyFire();
		IMove last = g.getAlienMoves().get(g.getAlienMoves().size() - 1);
		assertAll(
				() -> assertEquals(3, last.getShots().size(), "Error: expected 3 shots"),
				() -> assertTrue(last.getShots().stream().allMatch(p -> p.equals(new Position(9, 9))),
						"Error: expected all shots at J10 but got " + last.getShots())
		);
	}

	@Test
	@DisplayName("randomEnemyFire: sem posições livres dispara null e o assert de fireSingleShot falha")
	void randomEnemyFire4() {
		Game g = new Game(new Fleet());
		fireBoard(g, 0, 100);
		assertThrows(AssertionError.class, g::randomEnemyFire,
				"Error: expected AssertionError when there are no free positions");
	}

	// ---------- readEnemyFire (CC = 6, +1 assert) ----------

	@Test
	@DisplayName("readEnemyFire: Scanner nulo dispara o assert")
	void readEnemyFire1() {
		assertThrows(AssertionError.class, () -> game.readEnemyFire(null), "Error: expected AssertionError for null");
	}

	@Test
	@DisplayName("readEnemyFire: formato junto 'A1 B2 C3'")
	void readEnemyFire2() {
		String json = game.readEnemyFire(new Scanner("A1 B2 C3"));
		assertAll(
				() -> assertEquals(1, game.getAlienMoves().size(), "Error: expected 1 alien move"),
				() -> assertEquals(new Position(1, 1), game.getAlienMoves().get(0).getShots().get(1),
						"Error: expected the second shot at B2"),
				() -> assertTrue(json.contains("\"row\" : \"C\""), "Error: expected C in the JSON")
		);
	}

	@Test
	@DisplayName("readEnemyFire: formato separado 'A 1 B 2 C 3' (letra seguida de número)")
	void readEnemyFire3() {
		game.readEnemyFire(new Scanner("A 1 B 2 C 3"));
		assertEquals(shots(new Position(0, 0), new Position(1, 1), new Position(2, 2)),
				game.getAlienMoves().get(0).getShots(), "Error: expected shots A1, B2, C3");
	}

	@Test
	@DisplayName("readEnemyFire: letra sem número lança IllegalArgumentException")
	void readEnemyFire4() {
		assertThrows(IllegalArgumentException.class, () -> game.readEnemyFire(new Scanner("A X")),
				"Error: expected IllegalArgumentException for an incomplete position");
	}

	@Test
	@DisplayName("readEnemyFire: só 2 posições lança IllegalArgumentException")
	void readEnemyFire5() {
		assertThrows(IllegalArgumentException.class, () -> game.readEnemyFire(new Scanner("A1 B2")),
				"Error: expected IllegalArgumentException for 2 positions");
	}

	@Test
	@DisplayName("readEnemyFire: mais de 3 posições lê só as 3 primeiras")
	void readEnemyFire6() {
		game.readEnemyFire(new Scanner("A1 B2 C3 D4"));
		assertEquals(3, game.getAlienMoves().get(0).getShots().size(), "Error: expected only 3 shots read");
	}

	// ---------- fireShots (CC = 3, +1 assert) ----------

	@Test
	@DisplayName("fireShots: lista nula dispara o assert")
	void fireShots1() {
		assertThrows(AssertionError.class, () -> game.fireShots(null), "Error: expected AssertionError for null");
	}

	@Test
	@DisplayName("fireShots: número de tiros diferente de 3 lança IllegalArgumentException")
	void fireShots2() {
		assertThrows(IllegalArgumentException.class, () -> game.fireShots(shots(new Position(1, 1), new Position(2, 2))),
				"Error: expected IllegalArgumentException for 2 shots");
	}

	@Test
	@DisplayName("fireShots: tiro repetido dentro da mesma rajada conta como repetido")
	void fireShots3() {
		game.fireShots(shots(new Position(3, 3), new Position(3, 3), new Position(4, 4)));
		assertAll(
				() -> assertEquals(1, game.getRepeatedShots(), "Error: expected 1 repeated shot"),
				() -> assertEquals(1, game.getAlienMoves().size(), "Error: expected 1 alien move")
		);
	}

	// ---------- fireSingleShot (CC = 6, +1 assert) ----------

	@Test
	@DisplayName("fireSingleShot: posição nula dispara o assert")
	void fireSingleShot1() {
		assertThrows(AssertionError.class, () -> game.fireSingleShot(null, false),
				"Error: expected AssertionError for null");
	}

	@Test
	@DisplayName("fireSingleShot: posição fora do tabuleiro é inválida")
	void fireSingleShot2() {
		IGame.ShotResult r = game.fireSingleShot(new Position(-1, 3), false);
		assertAll(
				() -> assertFalse(r.valid(), "Error: expected an invalid shot"),
				() -> assertEquals(1, game.getInvalidShots(), "Error: expected 1 invalid shot")
		);
	}

	@Test
	@DisplayName("fireSingleShot: isRepeated = true conta como repetido")
	void fireSingleShot3() {
		IGame.ShotResult r = game.fireSingleShot(new Position(3, 3), true);
		assertAll(
				() -> assertTrue(r.repeated(), "Error: expected a repeated shot"),
				() -> assertEquals(1, game.getRepeatedShots(), "Error: expected 1 repeated shot")
		);
	}

	@Test
	@DisplayName("fireSingleShot: posição já disparada numa rajada anterior conta como repetido")
	void fireSingleShot4() {
		game.fireShots(shots(new Position(9, 9), new Position(9, 8), new Position(9, 7)));
		IGame.ShotResult r = game.fireSingleShot(new Position(9, 9), false);
		assertTrue(r.repeated(), "Error: expected a repeated shot (fired in a previous move)");
	}

	@Test
	@DisplayName("fireSingleShot: tiro na água é válido e não atinge navio")
	void fireSingleShot5() {
		IGame.ShotResult r = game.fireSingleShot(new Position(3, 3), false);
		assertAll(
				() -> assertTrue(r.valid(), "Error: expected a valid shot"),
				() -> assertFalse(r.repeated(), "Error: expected a non-repeated shot"),
				() -> assertNull(r.ship(), "Error: expected no ship hit"),
				() -> assertEquals(0, game.getHits(), "Error: expected 0 hits")
		);
	}

	@Test
	@DisplayName("fireSingleShot: acerto num navio que continua a flutuar")
	void fireSingleShot6() {
		IGame.ShotResult r = game.fireSingleShot(new Position(5, 5), false);
		assertAll(
				() -> assertSame(caravel, r.ship(), "Error: expected the caravel to be hit"),
				() -> assertFalse(r.sunk(), "Error: expected the caravel not to be sunk"),
				() -> assertEquals(1, game.getHits(), "Error: expected 1 hit"),
				() -> assertEquals(0, game.getSunkShips(), "Error: expected 0 sunk ships")
		);
	}

	@Test
	@DisplayName("fireSingleShot: acerto que afunda o navio")
	void fireSingleShot7() {
		IGame.ShotResult r = game.fireSingleShot(new Position(0, 0), false);
		assertAll(
				() -> assertSame(barge, r.ship(), "Error: expected the barge to be hit"),
				() -> assertTrue(r.sunk(), "Error: expected the barge to be sunk"),
				() -> assertEquals(1, game.getSunkShips(), "Error: expected 1 sunk ship")
		);
	}

	// ---------- counters (CC = 1) ----------

	@Test
	@DisplayName("getRepeatedShots: conta os tiros repetidos")
	void getRepeatedShots() {
		game.fireSingleShot(new Position(1, 1), true);
		game.fireSingleShot(new Position(2, 2), true);
		assertEquals(2, game.getRepeatedShots(), "Error: expected 2 repeated shots");
	}

	@Test
	@DisplayName("getInvalidShots: conta os tiros fora do tabuleiro")
	void getInvalidShots() {
		game.fireSingleShot(new Position(10, 0), false);
		assertEquals(1, game.getInvalidShots(), "Error: expected 1 invalid shot");
	}

	@Test
	@DisplayName("getHits: conta os acertos")
	void getHits() {
		game.fireSingleShot(new Position(5, 5), false);
		game.fireSingleShot(new Position(6, 5), false);
		assertEquals(2, game.getHits(), "Error: expected 2 hits");
	}

	@Test
	@DisplayName("getSunkShips: conta os navios afundados")
	void getSunkShips() {
		game.fireSingleShot(new Position(5, 5), false);
		game.fireSingleShot(new Position(6, 5), false);
		assertEquals(1, game.getSunkShips(), "Error: expected 1 sunk ship (the caravel)");
	}

	@Test
	@DisplayName("getRemainingShips: diminui quando um navio é afundado")
	void getRemainingShips() {
		int before = game.getRemainingShips();
		game.fireSingleShot(new Position(0, 0), false);
		assertAll(
				() -> assertEquals(2, before, "Error: expected 2 floating ships at the start"),
				() -> assertEquals(1, game.getRemainingShips(), "Error: expected 1 floating ship after sinking the barge")
		);
	}

	// ---------- repeatedShot (CC = 3, +1 assert) ----------

	@Test
	@DisplayName("repeatedShot: posição nula dispara o assert")
	void repeatedShot1() {
		assertThrows(AssertionError.class, () -> game.repeatedShot(null), "Error: expected AssertionError for null");
	}

	@Test
	@DisplayName("repeatedShot: sem jogadas devolve false")
	void repeatedShot2() {
		assertFalse(game.repeatedShot(new Position(1, 1)), "Error: expected false with no moves");
	}

	@Test
	@DisplayName("repeatedShot: posição disparada numa jogada anterior devolve true")
	void repeatedShot3() {
		game.fireShots(shots(new Position(9, 9), new Position(9, 8), new Position(9, 7)));
		assertTrue(game.repeatedShot(new Position(9, 8)), "Error: expected true for a previous shot");
	}

	@Test
	@DisplayName("repeatedShot: posição nunca disparada devolve false mesmo havendo jogadas")
	void repeatedShot4() {
		game.fireShots(shots(new Position(9, 9), new Position(9, 8), new Position(9, 7)));
		assertFalse(game.repeatedShot(new Position(1, 1)), "Error: expected false for a new position");
	}

	// ---------- printMyBoard / printAlienBoard / over (CC = 1) ----------

	@Test
	@DisplayName("printMyBoard: mostra o meu tabuleiro com os meus navios")
	void printMyBoard() {
		game.printMyBoard(true, true);
		assertAll(
				() -> assertEquals('#', cell(0, 0), "Error: expected my barge at A1"),
				() -> assertTrue(out.toString().contains("LEGENDA"), "Error: expected the legend")
		);
	}

	@Test
	@DisplayName("printAlienBoard: mostra o tabuleiro do adversário (vazio)")
	void printAlienBoard() {
		game.printAlienBoard(false, false);
		assertEquals('.', cell(0, 0), "Error: expected water at A1 on the alien board");
	}

	@Test
	@DisplayName("over: mostra a mensagem de fim de jogo")
	void over() {
		game.over();
		assertTrue(out.toString().contains("Java Sparrow"), "Error: expected the game over message");
	}
}