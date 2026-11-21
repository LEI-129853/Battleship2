package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Position.
 * Author: LEI-129820
 * Date: 2026-11-21
 * Cyclomatic Complexity:
 * - randomPosition(): 1
 * - Position(char, int): 1
 * - Position(int, int): 1
 * - getRow(): 1
 * - getColumn(): 1
 * - getClassicRow(): 1
 * - getClassicColumn(): 1
 * - isInside(): 4
 * - isAdjacentTo(): 2
 * - adjacentPositions(): 3
 * - isOccupied(): 1
 * - isHit(): 1
 * - occupy(): 1
 * - shoot(): 1
 * - equals(): 4
 * - hashCode(): 1
 * - toString(): 1
 */
public class PositionTest {

	private Position position;

	@BeforeEach
	void setUp() {
		position = new Position(2, 3);
	}

	@AfterEach
	void tearDown() {
		position = null;
	}

	// ---------- randomPosition (CC = 1) ----------

	@Test
	@DisplayName("randomPosition: gera sempre uma posição dentro do tabuleiro")
	void randomPosition() {
		for (int i = 0; i < 50; i++) {
			Position p = Position.randomPosition();
			assertTrue(p.isInside(), "Error: expected a position inside the board but got " + p);
		}
	}

	// ---------- constructors (CC = 1) ----------

	@Test
	@DisplayName("Position(char, int): converte coordenadas clássicas (minúscula incluída)")
	void constructorClassic() {
		Position p = new Position('c', 4);
		assertAll(
				() -> assertEquals(2, p.getRow(), "Error: expected row 2 but got " + p.getRow()),
				() -> assertEquals(3, p.getColumn(), "Error: expected column 3 but got " + p.getColumn()),
				() -> assertFalse(p.isOccupied(), "Error: expected a new position not to be occupied"),
				() -> assertFalse(p.isHit(), "Error: expected a new position not to be hit")
		);
	}

	@Test
	@DisplayName("Position(int, int): guarda linha e coluna, sem ocupação nem tiro")
	void constructor() {
		assertAll(
				() -> assertEquals(2, position.getRow(), "Error: expected row 2 but got " + position.getRow()),
				() -> assertEquals(3, position.getColumn(), "Error: expected column 3 but got " + position.getColumn()),
				() -> assertFalse(position.isOccupied(), "Error: expected a new position not to be occupied"),
				() -> assertFalse(position.isHit(), "Error: expected a new position not to be hit")
		);
	}

	// ---------- getters (CC = 1) ----------

	@Test
	@DisplayName("getRow: devolve a linha")
	void getRow() {
		assertEquals(2, position.getRow(), "Error: expected 2 but got " + position.getRow());
	}

	@Test
	@DisplayName("getColumn: devolve a coluna")
	void getColumn() {
		assertEquals(3, position.getColumn(), "Error: expected 3 but got " + position.getColumn());
	}

	@Test
	@DisplayName("getClassicRow: linha 2 corresponde à letra C")
	void getClassicRow() {
		assertEquals('C', position.getClassicRow(), "Error: expected 'C' but got " + position.getClassicRow());
	}

	@Test
	@DisplayName("getClassicColumn: coluna 3 corresponde ao número 4")
	void getClassicColumn() {
		assertEquals(4, position.getClassicColumn(), "Error: expected 4 but got " + position.getClassicColumn());
	}

	// ---------- isInside (CC = 4) ----------

	@Test
	@DisplayName("isInside: posição dentro do tabuleiro")
	void isInside1() {
		assertTrue(position.isInside(), "Error: expected (2,3) to be inside");
	}

	@Test
	@DisplayName("isInside: linha negativa")
	void isInside2() {
		assertFalse(new Position(-1, 3).isInside(), "Error: expected (-1,3) to be outside");
	}

	@Test
	@DisplayName("isInside: coluna negativa")
	void isInside3() {
		assertFalse(new Position(2, -1).isInside(), "Error: expected (2,-1) to be outside");
	}

	@Test
	@DisplayName("isInside: linha igual ao tamanho do tabuleiro")
	void isInside4() {
		assertFalse(new Position(Game.BOARD_SIZE, 3).isInside(), "Error: expected (10,3) to be outside");
	}

	@Test
	@DisplayName("isInside: coluna igual ao tamanho do tabuleiro")
	void isInside5() {
		assertFalse(new Position(2, Game.BOARD_SIZE).isInside(), "Error: expected (2,10) to be outside");
	}

	// ---------- isAdjacentTo (CC = 2) ----------

	@Test
	@DisplayName("isAdjacentTo: posição na diagonal é adjacente")
	void isAdjacentTo1() {
		assertTrue(position.isAdjacentTo(new Position(3, 4)), "Error: expected (3,4) to be adjacent to (2,3)");
	}

	@Test
	@DisplayName("isAdjacentTo: linha a mais de 1 de distância não é adjacente")
	void isAdjacentTo2() {
		assertFalse(position.isAdjacentTo(new Position(4, 3)), "Error: expected (4,3) not to be adjacent");
	}

	@Test
	@DisplayName("isAdjacentTo: mesma linha mas coluna a mais de 1 de distância não é adjacente")
	void isAdjacentTo3() {
		assertFalse(position.isAdjacentTo(new Position(2, 5)), "Error: expected (2,5) not to be adjacent");
	}

	@Test
	@DisplayName("isAdjacentTo: posição nula lança NullPointerException")
	void isAdjacentTo4() {
		assertThrows(NullPointerException.class, () -> position.isAdjacentTo(null),
				"Error: expected NullPointerException for null");
	}

	// ---------- adjacentPositions (CC = 3) ----------

	@Test
	@DisplayName("adjacentPositions: posição no meio tem 8 adjacentes")
	void adjacentPositions1() {
		List<IPosition> adj = position.adjacentPositions();
		assertEquals(8, adj.size(), "Error: expected 8 adjacent positions but got " + adj.size());
	}

	@Test
	@DisplayName("adjacentPositions: canto (0,0) só tem 3 adjacentes (as outras ficam fora)")
	void adjacentPositions2() {
		List<IPosition> adj = new Position(0, 0).adjacentPositions();
		assertAll(
				() -> assertEquals(3, adj.size(), "Error: expected 3 adjacent positions but got " + adj.size()),
				() -> assertTrue(adj.contains(new Position(1, 1)), "Error: expected (1,1) to be adjacent")
		);
	}

	// ---------- isOccupied / occupy / isHit / shoot (CC = 1) ----------

	@Test
	@DisplayName("isOccupied: falso numa posição nova")
	void isOccupied() {
		assertFalse(position.isOccupied(), "Error: expected a new position not to be occupied");
	}

	@Test
	@DisplayName("occupy: marca a posição como ocupada")
	void occupy() {
		position.occupy();
		assertTrue(position.isOccupied(), "Error: expected the position to be occupied");
	}

	@Test
	@DisplayName("isHit: falso numa posição nova")
	void isHit() {
		assertFalse(position.isHit(), "Error: expected a new position not to be hit");
	}

	@Test
	@DisplayName("shoot: marca a posição como atingida")
	void shoot() {
		position.shoot();
		assertTrue(position.isHit(), "Error: expected the position to be hit");
	}

	// ---------- equals (CC = 4) ----------

	@Test
	@DisplayName("equals: o próprio objeto")
	void equals1() {
		assertTrue(position.equals(position), "Error: expected a position to be equal to itself");
	}

	@Test
	@DisplayName("equals: outra posição com a mesma linha e coluna")
	void equals2() {
		assertTrue(position.equals(new Position(2, 3)), "Error: expected (2,3) to be equal to (2,3)");
	}

	@Test
	@DisplayName("equals: linha diferente")
	void equals3() {
		assertFalse(position.equals(new Position(5, 3)), "Error: expected (5,3) to be different from (2,3)");
	}

	@Test
	@DisplayName("equals: mesma linha, coluna diferente")
	void equals4() {
		assertFalse(position.equals(new Position(2, 7)), "Error: expected (2,7) to be different from (2,3)");
	}

	@Test
	@DisplayName("equals: objeto que não é IPosition (e null)")
	void equals5() {
		assertAll(
				() -> assertFalse(position.equals("C4"), "Error: expected a Position not to be equal to a String"),
				() -> assertFalse(position.equals(null), "Error: expected a Position not to be equal to null")
		);
	}

	// ---------- hashCode (CC = 1) ----------

	@Test
	@DisplayName("hashCode: posições iguais e no mesmo estado têm o mesmo hash")
	void testHashCode() {
		assertEquals(new Position(2, 3).hashCode(), position.hashCode(),
				"Error: expected equal positions to have the same hash code");
	}

	// ---------- toString (CC = 1) ----------

	@Test
	@DisplayName("toString: usa o formato clássico (letra + número)")
	void testToString() {
		assertEquals("C4", position.toString(), "Error: expected 'C4' but got " + position);
	}
}