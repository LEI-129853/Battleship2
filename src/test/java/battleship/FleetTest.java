package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Fleet.
 * Author: LEI-129820
 * Date: 2026-11-21
 * Cyclomatic Complexity:
 * - createRandom(): 4
 * - constructor: 1
 * - getShips(): 1
 * - addShip(): 4 (+1 assert)
 * - getShipsLike(): 3 (+1 assert)
 * - getFloatingShips(): 3
 * - getSunkShips(): 3
 * - shipAt(): 3 (+1 assert)
 * - colisionRisk() (private): 3 (+1 assert) - covered through addShip()
 * - printShips(): 2 (+1 assert)
 * - printStatus(): 1
 * - printShipsByCategory(): 1 (+1 assert)
 * - printFloatingShips(): 1
 * - printAllShips(): 1
 * Notes:
 * - createRandom(): "ship != null" is always true (all kinds in the array are valid) - its false branch is unreachable.
 * - colisionRisk(): its assert can never fail (addShip() already checks s != null).
 */
public class FleetTest {

	private Fleet fleet;
	private PrintStream originalOut;
	private ByteArrayOutputStream out;

	@BeforeEach
	void setUp() {
		fleet = new Fleet();
		originalOut = System.out;
		out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
	}

	@AfterEach
	void tearDown() {
		System.setOut(originalOut);
		fleet = null;
		out = null;
	}

	// ---------- createRandom (CC = 4) ----------

	@Test
	@DisplayName("createRandom: cria uma frota com os 11 navios, todos dentro do tabuleiro")
	void createRandom1() {
		IFleet f = Fleet.createRandom();
		assertAll(
				() -> assertEquals(IFleet.FLEET_SIZE, f.getShips().size(),
						"Error: expected 11 ships but got " + f.getShips().size()),
				() -> assertTrue(f.getShips().stream().allMatch(IShip::isInsideBoard),
						"Error: expected all ships inside the board")
		);
	}

	@Test
	@DisplayName("createRandom: a frota tem 1 galeão, 1 fragata, 2 naus, 3 caravelas e 4 barcas")
	void createRandom2() {
		IFleet f = Fleet.createRandom();
		assertAll(
				() -> assertEquals(1, f.getShipsLike("Galeao").size(), "Error: expected 1 Galeao"),
				() -> assertEquals(1, f.getShipsLike("Fragata").size(), "Error: expected 1 Fragata"),
				() -> assertEquals(2, f.getShipsLike("Nau").size(), "Error: expected 2 Nau"),
				() -> assertEquals(3, f.getShipsLike("Caravela").size(), "Error: expected 3 Caravela"),
				() -> assertEquals(4, f.getShipsLike("Barca").size(), "Error: expected 4 Barca")
		);
	}

	@Test
	@DisplayName("createRandom: em várias frotas aleatórias, nenhum navio fica encostado a outro")
	void createRandom3() {
		for (int k = 0; k < 20; k++) {
			List<IShip> ships = Fleet.createRandom().getShips();
			for (int i = 0; i < ships.size(); i++)
				for (int j = i + 1; j < ships.size(); j++)
					assertFalse(ships.get(i).tooCloseTo(ships.get(j)),
							"Error: expected no ships too close, but " + ships.get(i) + " and " + ships.get(j) + " are");
		}
	}

	// ---------- constructor / getShips (CC = 1) ----------

	@Test
	@DisplayName("constructor: frota nova começa vazia")
	void constructor() {
		assertNotNull(fleet.getShips(), "Error: expected a list of ships, not null");
	}

	@Test
	@DisplayName("getShips: devolve os navios adicionados")
	void getShips() {
		IShip barge = new Barge(Compass.NORTH, new Position(0, 0));
		fleet.addShip(barge);
		assertEquals(List.of(barge), fleet.getShips(), "Error: expected exactly the added barge");
	}

	// ---------- addShip (CC = 4, +1 assert) ----------

	@Test
	@DisplayName("addShip: navio válido, dentro do tabuleiro e sem colisão, é adicionado")
	void addShip1() {
		assertAll(
				() -> assertTrue(fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0))), "Error: expected true"),
				() -> assertEquals(1, fleet.getShips().size(), "Error: expected 1 ship")
		);
	}

	@Test
	@DisplayName("addShip: navio fora do tabuleiro é rejeitado")
	void addShip2() {
		assertAll(
				() -> assertFalse(fleet.addShip(new Barge(Compass.NORTH, new Position(10, 10))), "Error: expected false"),
				() -> assertTrue(fleet.getShips().isEmpty(), "Error: expected no ships")
		);
	}

	@Test
	@DisplayName("addShip: navio encostado a outro (colisão) é rejeitado")
	void addShip3() {
		fleet.addShip(new Barge(Compass.NORTH, new Position(5, 5)));
		assertAll(
				() -> assertFalse(fleet.addShip(new Barge(Compass.NORTH, new Position(5, 6))), "Error: expected false"),
				() -> assertEquals(1, fleet.getShips().size(), "Error: expected only the first ship")
		);
	}

	@Test
	@DisplayName("addShip: com a frota já acima de FLEET_SIZE, o navio é rejeitado")
	void addShip4() {
		// 12 barges spaced 2 apart: ships.size() <= FLEET_SIZE (11) still lets the 12th in
		int added = 0;
		for (int r = 0; r < Game.BOARD_SIZE && added < 12; r += 2)
			for (int c = 0; c < Game.BOARD_SIZE && added < 12; c += 2)
				if (fleet.addShip(new Barge(Compass.NORTH, new Position(r, c))))
					added++;
		assertAll(
				() -> assertEquals(12, fleet.getShips().size(), "Error: expected 12 ships before the full check"),
				() -> assertFalse(fleet.addShip(new Barge(Compass.NORTH, new Position(8, 8))),
						"Error: expected false when the fleet is full")
		);
	}

	@Test
	@DisplayName("addShip: navio nulo dispara o assert")
	void addShip5() {
		assertThrows(AssertionError.class, () -> fleet.addShip(null), "Error: expected AssertionError for null");
	}

	// ---------- getShipsLike (CC = 3, +1 assert) ----------

	@Test
	@DisplayName("getShipsLike: devolve só os navios da categoria pedida")
	void getShipsLike1() {
		fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0)));
		fleet.addShip(new Caravel(Compass.NORTH, new Position(5, 5)));
		List<IShip> barges = fleet.getShipsLike("Barca");
		assertAll(
				() -> assertEquals(1, barges.size(), "Error: expected 1 Barca but got " + barges.size()),
				() -> assertEquals("Barca", barges.get(0).getCategory(), "Error: expected a Barca")
		);
	}

	@Test
	@DisplayName("getShipsLike: categoria sem navios devolve lista vazia")
	void getShipsLike2() {
		fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0)));
		assertTrue(fleet.getShipsLike("Galeao").isEmpty(), "Error: expected no Galeao");
	}

	@Test
	@DisplayName("getShipsLike: categoria nula dispara o assert")
	void getShipsLike3() {
		assertThrows(AssertionError.class, () -> fleet.getShipsLike(null), "Error: expected AssertionError for null");
	}

	// ---------- getFloatingShips (CC = 3) ----------

	@Test
	@DisplayName("getFloatingShips: devolve só os navios a flutuar")
	void getFloatingShips1() {
		IShip floating = new Barge(Compass.NORTH, new Position(0, 0));
		IShip sunk = new Barge(Compass.NORTH, new Position(5, 5));
		fleet.addShip(floating);
		fleet.addShip(sunk);
		sunk.sink();
		assertEquals(List.of(floating), fleet.getFloatingShips(), "Error: expected only the floating barge");
	}

	@Test
	@DisplayName("getFloatingShips: frota vazia devolve lista vazia")
	void getFloatingShips2() {
		assertTrue(fleet.getFloatingShips().isEmpty(), "Error: expected no floating ships");
	}

	// ---------- getSunkShips (CC = 3) ----------

	@Test
	@DisplayName("getSunkShips: devolve só os navios afundados")
	void getSunkShips1() {
		IShip floating = new Barge(Compass.NORTH, new Position(0, 0));
		IShip sunk = new Barge(Compass.NORTH, new Position(5, 5));
		fleet.addShip(floating);
		fleet.addShip(sunk);
		sunk.sink();
		assertEquals(List.of(sunk), fleet.getSunkShips(), "Error: expected only the sunk barge");
	}

	@Test
	@DisplayName("getSunkShips: frota vazia devolve lista vazia")
	void getSunkShips2() {
		assertTrue(fleet.getSunkShips().isEmpty(), "Error: expected no sunk ships");
	}

	// ---------- shipAt (CC = 3, +1 assert) ----------

	@Test
	@DisplayName("shipAt: devolve o navio que ocupa a posição")
	void shipAt1() {
		IShip caravel = new Caravel(Compass.NORTH, new Position(5, 5));
		fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0)));
		fleet.addShip(caravel);
		assertSame(caravel, fleet.shipAt(new Position(6, 5)), "Error: expected the caravel at (6,5)");
	}

	@Test
	@DisplayName("shipAt: posição sem navio devolve null")
	void shipAt2() {
		fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0)));
		assertNull(fleet.shipAt(new Position(9, 9)), "Error: expected no ship at (9,9)");
	}

	@Test
	@DisplayName("shipAt: posição nula dispara o assert")
	void shipAt3() {
		assertThrows(AssertionError.class, () -> fleet.shipAt(null), "Error: expected AssertionError for null");
	}

	// ---------- printShips (CC = 2, +1 assert) ----------

	@Test
	@DisplayName("printShips: imprime um navio por linha")
	void printShips1() {
		fleet.printShips(List.of(new Barge(Compass.NORTH, new Position(0, 0)), new Barge(Compass.NORTH, new Position(5, 5))));
		assertEquals(2, out.toString().lines().count(), "Error: expected 2 printed lines but got: " + out);
	}

	@Test
	@DisplayName("printShips: lista vazia não imprime nada")
	void printShips2() {
		fleet.printShips(List.of());
		assertEquals("", out.toString(), "Error: expected nothing printed but got: " + out);
	}

	@Test
	@DisplayName("printShips: lista nula dispara o assert")
	void printShips3() {
		assertThrows(AssertionError.class, () -> fleet.printShips(null), "Error: expected AssertionError for null");
	}

	// ---------- printStatus / printShipsByCategory / printFloatingShips / printAllShips ----------

	@Test
	@DisplayName("printStatus: mostra quantos navios flutuam e quantos afundaram")
	void printStatus() {
		IShip sunk = new Barge(Compass.NORTH, new Position(5, 5));
		fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0)));
		fleet.addShip(sunk);
		sunk.sink();
		fleet.printStatus();
		assertTrue(out.toString().contains("Estado da Frota: 1 a flutuar, 1 afundados!"),
				"Error: expected '1 a flutuar, 1 afundados' but got: " + out);
	}

	@Test
	@DisplayName("printShipsByCategory: imprime só os navios da categoria")
	void printShipsByCategory1() {
		fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0)));
		fleet.addShip(new Caravel(Compass.NORTH, new Position(5, 5)));
		fleet.printShipsByCategory("Caravela");
		assertAll(
				() -> assertTrue(out.toString().contains("Caravela"), "Error: expected the caravel printed"),
				() -> assertFalse(out.toString().contains("Barca"), "Error: expected no barge printed")
		);
	}

	@Test
	@DisplayName("printShipsByCategory: categoria nula dispara o assert")
	void printShipsByCategory2() {
		assertThrows(AssertionError.class, () -> fleet.printShipsByCategory(null),
				"Error: expected AssertionError for null");
	}

	@Test
	@DisplayName("printFloatingShips: imprime só os navios a flutuar")
	void printFloatingShips() {
		IShip sunk = new Caravel(Compass.NORTH, new Position(5, 5));
		fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0)));
		fleet.addShip(sunk);
		sunk.sink();
		fleet.printFloatingShips();
		assertAll(
				() -> assertTrue(out.toString().contains("Barca"), "Error: expected the floating barge printed"),
				() -> assertFalse(out.toString().contains("Caravela"), "Error: expected the sunk caravel not printed")
		);
	}

	@Test
	@DisplayName("printAllShips: imprime todos os navios")
	void printAllShips() {
		fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0)));
		fleet.addShip(new Caravel(Compass.NORTH, new Position(5, 5)));
		fleet.printAllShips();
		assertEquals(2, out.toString().lines().count(), "Error: expected 2 printed lines but got: " + out);
	}
}