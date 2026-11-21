package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Ship.
 * Author: LEI-129853
 * Date: 2026-11-21
 * Cyclomatic Complexity:
 * - buildShip(): 6 (+3 asserts)
 * - constructor: 1
 * - getCategory(): 1
 * - getPositions(): 1
 * - getAdjacentPositions(): 5
 * - getPosition(): 1
 * - getBearing(): 1
 * - getSize(): 1
 * - stillFloating(): 3
 * - getTopMostPos(): 3
 * - getBottomMostPos(): 3
 * - getLeftMostPos(): 3
 * - getRightMostPos(): 3
 * - occupies(): 3 (+1 assert)
 * - tooCloseTo(IShip): 3 (+1 assert)
 * - tooCloseTo(IPosition): 3 (+1 assert)
 * - isInsideBoard(): 4
 * - shoot(): 3 (+2 asserts)
 * - sink(): 2
 * - toString(): 1
 * Notes:
 * - Ship is abstract: the instance under test is a Carrack (size 3, vertical) and, where a specific
 *   order of positions is needed, an anonymous subclass of Ship ("custom ship").
 * - "assert this != null" in isInsideBoard() can never fail (unreachable branch).
 */
public class ShipTest {

    /** Carrack NORTH at (5,5): occupies (5,5), (6,5) and (7,5). */
    private Ship ship;

    @BeforeEach
    void setUp() {
        ship = new Carrack(Compass.NORTH, new Position(5, 5));
    }

    @AfterEach
    void tearDown() {
        ship = null;
    }

    /** Ship whose positions are added in the given order (to control the min/max loops). */
    private static Ship custom(IPosition... positions) {
        Ship s = new Ship("Teste", Compass.NORTH, new Position(0, 0), positions.length) { };
        s.getPositions().addAll(List.of(positions));
        return s;
    }

    // ---------- buildShip (CC = 6, +3 asserts) ----------

    @Test
    @DisplayName("buildShip: 'barca' cria uma Barge")
    void buildShip1() {
        assertInstanceOf(Barge.class, Ship.buildShip("barca", Compass.NORTH, new Position(1, 1)),
                "Error: expected a Barge");
    }

    @Test
    @DisplayName("buildShip: 'caravela' cria uma Caravel")
    void buildShip2() {
        assertInstanceOf(Caravel.class, Ship.buildShip("caravela", Compass.NORTH, new Position(1, 1)),
                "Error: expected a Caravel");
    }

    @Test
    @DisplayName("buildShip: 'nau' cria uma Carrack")
    void buildShip3() {
        assertInstanceOf(Carrack.class, Ship.buildShip("nau", Compass.NORTH, new Position(1, 1)),
                "Error: expected a Carrack");
    }

    @Test
    @DisplayName("buildShip: 'fragata' cria uma Frigate")
    void buildShip4() {
        assertInstanceOf(Frigate.class, Ship.buildShip("fragata", Compass.NORTH, new Position(1, 1)),
                "Error: expected a Frigate");
    }

    @Test
    @DisplayName("buildShip: 'galeao' cria um Galleon")
    void buildShip5() {
        assertInstanceOf(Galleon.class, Ship.buildShip("galeao", Compass.NORTH, new Position(1, 1)),
                "Error: expected a Galleon");
    }

    @Test
    @DisplayName("buildShip: tipo desconhecido devolve null (default)")
    void buildShip6() {
        assertNull(Ship.buildShip("xpto", Compass.NORTH, new Position(1, 1)),
                "Error: expected null for an unknown ship kind");
    }

    @Test
    @DisplayName("buildShip: tipo, rumo ou posição nulos disparam os asserts")
    void buildShip7() {
        assertAll(
                () -> assertThrows(AssertionError.class, () -> Ship.buildShip(null, Compass.NORTH, new Position(1, 1)),
                        "Error: expected AssertionError for null kind"),
                () -> assertThrows(AssertionError.class, () -> Ship.buildShip("barca", null, new Position(1, 1)),
                        "Error: expected AssertionError for null bearing"),
                () -> assertThrows(AssertionError.class, () -> Ship.buildShip("barca", Compass.NORTH, null),
                        "Error: expected AssertionError for null position")
        );
    }

    // ---------- constructor and getters (CC = 1) ----------

    @Test
    @DisplayName("constructor: categoria, rumo ou posição nulos lançam NullPointerException")
    void constructor() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new Ship(null, Compass.NORTH, new Position(0, 0), 1) { },
                        "Error: expected NullPointerException for null category"),
                () -> assertThrows(NullPointerException.class, () -> new Ship("Teste", null, new Position(0, 0), 1) { },
                        "Error: expected NullPointerException for null bearing"),
                () -> assertThrows(NullPointerException.class, () -> new Ship("Teste", Compass.NORTH, null, 1) { },
                        "Error: expected NullPointerException for null position")
        );
    }

    @Test
    @DisplayName("getCategory: devolve a categoria do navio")
    void getCategory() {
        assertEquals("Nau", ship.getCategory(), "Error: expected 'Nau' but got " + ship.getCategory());
    }

    @Test
    @DisplayName("getPositions: devolve as 3 posições ocupadas")
    void getPositions() {
        assertEquals(List.of(new Position(5, 5), new Position(6, 5), new Position(7, 5)), ship.getPositions(),
                "Error: expected positions (5,5), (6,5), (7,5) but got " + ship.getPositions());
    }

    @Test
    @DisplayName("getPosition: devolve a posição inicial")
    void getPosition() {
        assertEquals(new Position(5, 5), ship.getPosition(), "Error: expected (5,5) but got " + ship.getPosition());
    }

    @Test
    @DisplayName("getBearing: devolve o rumo")
    void getBearing() {
        assertEquals(Compass.NORTH, ship.getBearing(), "Error: expected NORTH but got " + ship.getBearing());
    }

    @Test
    @DisplayName("getSize: devolve o tamanho")
    void getSize() {
        assertEquals(3, ship.getSize(), "Error: expected size 3 but got " + ship.getSize());
    }

    @Test
    @DisplayName("toString: mostra categoria, rumo e posição")
    void testToString() {
        String s = ship.toString();
        assertAll(
                () -> assertTrue(s.startsWith("[Nau"), "Error: expected to start with '[Nau' but got " + s),
                () -> assertTrue(s.endsWith("]"), "Error: expected to end with ']' but got " + s)
        );
    }

    // ---------- getAdjacentPositions (CC = 5) ----------

    @Test
    @DisplayName("getAdjacentPositions: navio no meio tem 12 adjacentes, sem repetidos nem posições do navio")
    void getAdjacentPositions1() {
        List<IPosition> adj = ship.getAdjacentPositions();
        assertAll(
                () -> assertEquals(12, adj.size(), "Error: expected 12 adjacent positions but got " + adj.size()),
                () -> assertFalse(adj.contains(new Position(6, 5)), "Error: a ship position cannot be adjacent"),
                () -> assertEquals(adj.size(), adj.stream().distinct().count(), "Error: expected no duplicates")
        );
    }

    @Test
    @DisplayName("getAdjacentPositions: barca no canto (0,0) tem só 3 adjacentes")
    void getAdjacentPositions2() {
        Ship barge = new Barge(Compass.NORTH, new Position(0, 0));
        assertEquals(3, barge.getAdjacentPositions().size(),
                "Error: expected 3 adjacent positions but got " + barge.getAdjacentPositions().size());
    }

    @Test
    @DisplayName("getAdjacentPositions: navio sem posições não entra no ciclo e devolve lista vazia")
    void getAdjacentPositions3() {
        assertTrue(custom().getAdjacentPositions().isEmpty(), "Error: expected no adjacent positions");
    }

    // ---------- stillFloating (CC = 3) ----------

    @Test
    @DisplayName("stillFloating: navio intacto flutua (primeira posição não atingida)")
    void stillFloating1() {
        assertTrue(ship.stillFloating(), "Error: expected the ship to be floating");
    }

    @Test
    @DisplayName("stillFloating: navio com as primeiras posições atingidas ainda flutua")
    void stillFloating2() {
        ship.getPositions().get(0).shoot();
        ship.getPositions().get(1).shoot();
        assertTrue(ship.stillFloating(), "Error: expected the ship to be floating with one intact position");
    }

    @Test
    @DisplayName("stillFloating: navio com todas as posições atingidas afunda")
    void stillFloating3() {
        ship.getPositions().forEach(IPosition::shoot);
        assertFalse(ship.stillFloating(), "Error: expected the ship to be sunk");
    }

    // ---------- getTopMostPos / getBottomMostPos / getLeftMostPos / getRightMostPos (CC = 3 each) ----------

    @Test
    @DisplayName("getTopMostPos: posições por ordem crescente (o if nunca atualiza)")
    void getTopMostPos1() {
        assertEquals(5, ship.getTopMostPos(), "Error: expected 5 but got " + ship.getTopMostPos());
    }

    @Test
    @DisplayName("getTopMostPos: posição mais acima vem no fim (o if atualiza)")
    void getTopMostPos2() {
        Ship s = custom(new Position(7, 2), new Position(3, 2));
        assertEquals(3, s.getTopMostPos(), "Error: expected 3 but got " + s.getTopMostPos());
    }

    @Test
    @DisplayName("getBottomMostPos: posição mais abaixo vem no fim (o if atualiza)")
    void getBottomMostPos1() {
        assertEquals(7, ship.getBottomMostPos(), "Error: expected 7 but got " + ship.getBottomMostPos());
    }

    @Test
    @DisplayName("getBottomMostPos: posições por ordem decrescente (o if nunca atualiza)")
    void getBottomMostPos2() {
        Ship s = custom(new Position(7, 2), new Position(3, 2));
        assertEquals(7, s.getBottomMostPos(), "Error: expected 7 but got " + s.getBottomMostPos());
    }

    @Test
    @DisplayName("getLeftMostPos: navio vertical (o if nunca atualiza)")
    void getLeftMostPos1() {
        assertEquals(5, ship.getLeftMostPos(), "Error: expected 5 but got " + ship.getLeftMostPos());
    }

    @Test
    @DisplayName("getLeftMostPos: coluna mais à esquerda vem no fim (o if atualiza)")
    void getLeftMostPos2() {
        Ship s = custom(new Position(2, 7), new Position(2, 3));
        assertEquals(3, s.getLeftMostPos(), "Error: expected 3 but got " + s.getLeftMostPos());
    }

    @Test
    @DisplayName("getRightMostPos: navio vertical (o if nunca atualiza)")
    void getRightMostPos1() {
        assertEquals(5, ship.getRightMostPos(), "Error: expected 5 but got " + ship.getRightMostPos());
    }

    @Test
    @DisplayName("getRightMostPos: coluna mais à direita vem no fim (o if atualiza)")
    void getRightMostPos2() {
        Ship s = custom(new Position(2, 3), new Position(2, 7));
        assertEquals(7, s.getRightMostPos(), "Error: expected 7 but got " + s.getRightMostPos());
    }

    // ---------- occupies (CC = 3, +1 assert) ----------

    @Test
    @DisplayName("occupies: posição do navio devolve true")
    void occupies1() {
        assertTrue(ship.occupies(new Position(6, 5)), "Error: expected the ship to occupy (6,5)");
    }

    @Test
    @DisplayName("occupies: posição fora do navio devolve false")
    void occupies2() {
        assertFalse(ship.occupies(new Position(0, 0)), "Error: expected the ship not to occupy (0,0)");
    }

    @Test
    @DisplayName("occupies: posição nula dispara o assert")
    void occupies3() {
        assertThrows(AssertionError.class, () -> ship.occupies(null), "Error: expected AssertionError for null");
    }

    // ---------- tooCloseTo(IShip) (CC = 3, +1 assert) ----------

    @Test
    @DisplayName("tooCloseTo(IShip): navio encostado devolve true")
    void tooCloseToShip1() {
        IShip other = new Barge(Compass.NORTH, new Position(8, 6));
        assertTrue(ship.tooCloseTo(other), "Error: expected the ships to be too close");
    }

    @Test
    @DisplayName("tooCloseTo(IShip): navio afastado devolve false")
    void tooCloseToShip2() {
        IShip other = new Caravel(Compass.NORTH, new Position(0, 0));
        assertFalse(ship.tooCloseTo(other), "Error: expected the ships not to be too close");
    }

    @Test
    @DisplayName("tooCloseTo(IShip): navio nulo dispara o assert")
    void tooCloseToShip3() {
        assertThrows(AssertionError.class, () -> ship.tooCloseTo((IShip) null),
                "Error: expected AssertionError for null ship");
    }

    // ---------- tooCloseTo(IPosition) (CC = 3, +1 assert) ----------

    @Test
    @DisplayName("tooCloseTo(IPosition): posição adjacente devolve true")
    void tooCloseToPosition1() {
        assertTrue(ship.tooCloseTo(new Position(4, 4)), "Error: expected (4,4) to be too close");
    }

    @Test
    @DisplayName("tooCloseTo(IPosition): posição afastada devolve false")
    void tooCloseToPosition2() {
        assertFalse(ship.tooCloseTo(new Position(0, 0)), "Error: expected (0,0) not to be too close");
    }

    @Test
    @DisplayName("tooCloseTo(IPosition): posição nula dispara o assert")
    void tooCloseToPosition3() {
        assertThrows(AssertionError.class, () -> ship.tooCloseTo((IPosition) null),
                "Error: expected AssertionError for null position");
    }

    // ---------- isInsideBoard (CC = 4) ----------

    @Test
    @DisplayName("isInsideBoard: navio dentro do tabuleiro devolve true")
    void isInsideBoard1() {
        assertTrue(ship.isInsideBoard(), "Error: expected the ship to be inside the board");
    }

    @Test
    @DisplayName("isInsideBoard: coluna à esquerda negativa devolve false")
    void isInsideBoard2() {
        assertFalse(new Barge(Compass.NORTH, new Position(5, -1)).isInsideBoard(), "Error: expected false for column -1");
    }

    @Test
    @DisplayName("isInsideBoard: coluna à direita para lá do limite devolve false")
    void isInsideBoard3() {
        assertFalse(new Barge(Compass.NORTH, new Position(5, 10)).isInsideBoard(), "Error: expected false for column 10");
    }

    @Test
    @DisplayName("isInsideBoard: linha de cima negativa devolve false")
    void isInsideBoard4() {
        assertFalse(new Barge(Compass.NORTH, new Position(-1, 5)).isInsideBoard(), "Error: expected false for row -1");
    }

    @Test
    @DisplayName("isInsideBoard: linha de baixo para lá do limite devolve false")
    void isInsideBoard5() {
        assertFalse(new Barge(Compass.NORTH, new Position(10, 5)).isInsideBoard(), "Error: expected false for row 10");
    }

    // ---------- shoot (CC = 3, +2 asserts) ----------

    @Test
    @DisplayName("shoot: tiro numa posição do navio atinge essa posição")
    void shoot1() {
        ship.shoot(new Position(6, 5));
        assertAll(
                () -> assertTrue(ship.getPositions().get(1).isHit(), "Error: expected (6,5) to be hit"),
                () -> assertFalse(ship.getPositions().get(0).isHit(), "Error: expected (5,5) not to be hit")
        );
    }

    @Test
    @DisplayName("shoot: tiro fora do navio não atinge nenhuma posição")
    void shoot2() {
        ship.shoot(new Position(0, 0));
        assertTrue(ship.getPositions().stream().noneMatch(IPosition::isHit), "Error: expected no position to be hit");
    }

    @Test
    @DisplayName("shoot: posição nula ou fora do tabuleiro dispara os asserts")
    void shoot3() {
        assertAll(
                () -> assertThrows(AssertionError.class, () -> ship.shoot(null), "Error: expected AssertionError for null"),
                () -> assertThrows(AssertionError.class, () -> ship.shoot(new Position(-1, 0)),
                        "Error: expected AssertionError for a position outside the board")
        );
    }

    // ---------- sink (CC = 2) ----------

    @Test
    @DisplayName("sink: atinge todas as posições e o navio deixa de flutuar")
    void sink1() {
        ship.sink();
        assertAll(
                () -> assertTrue(ship.getPositions().stream().allMatch(IPosition::isHit), "Error: expected all positions hit"),
                () -> assertFalse(ship.stillFloating(), "Error: expected the ship to be sunk")
        );
    }

    @Test
    @DisplayName("sink: navio sem posições não entra no ciclo")
    void sink2() {
        Ship s = custom();
        assertDoesNotThrow(s::sink, "Error: expected sink() on an empty ship not to throw");
    }
}