package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

// Imports any necessary classes like Position, Compass, etc.

import java.util.List;

/**
 * Test class for class Carrack.
 * Author: LEI-129851
 * Date: 2026-11-21
 * Cyclomatic Complexity:
 * - constructor: 9 (switch over 4 bearings, each with a loop)
 * - getSize(): 1
 * - stillFloating(): 3 (inherited from Ship)
 * - getTopMostPos(), getBottomMostPos(), getLeftMostPos(), getRightMostPos(): 3 (inherited from Ship)
 * Note: the switch over the Compass enum has an implicit default branch that is unreachable
 *       (Compass has only 4 values and a null bearing is rejected by Ship's constructor).
 */
public class CarrackTest {

    private Carrack carrack;

    @BeforeEach
    void setUp() {
        carrack = new Carrack(Compass.NORTH, new Position(5, 5));
    }

    @AfterEach
    void tearDown() {
        carrack = null;
    }

    /**
     * Test for the constructor with valid input.
     * Cyclomatic Complexity: 5
     */
    @Test
    @DisplayName("constructor: rumo NORTH cria o navio com os atributos corretos")
    void testConstructor() {
        assertNotNull(carrack, "Error: Carrack instance should not be null.");
        assertEquals("Nau", carrack.getCategory(), "Error: Carrack's category should be 'Nau'.");
        assertEquals(Compass.NORTH, carrack.getBearing(), "Error: Carrack's bearing is incorrect.");
        assertEquals(3, carrack.getSize(), "Error: Carrack's size should be 3.");
        assertEquals(3, carrack.getPositions().size(), "Error: Carrack should have 3 positions.");
    }

    /**
     * Test for the getSize method (basic validation).
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("getSize: devolve o tamanho do navio")
    void testGetSize() {
        assertEquals(3, carrack.getSize(), "Error: The size of the Carrack should be 3.");
    }

    /**
     * Test for the getPositions method (all positions must be correct).
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("getPositions: devolve as posições ocupadas")
    void testGetPositions() {
        List<IPosition> positions = carrack.getPositions();

        // Validate the number of positions
        assertEquals(3, positions.size(), "Error: The Carrack should have three positions.");

        // Validate each position
        assertEquals(new Position(5, 5), positions.get(0), "Error: The first position is incorrect.");
        assertEquals(new Position(6, 5), positions.get(1), "Error: The second position is incorrect.");
        assertEquals(new Position(7, 5), positions.get(2), "Error: The third position is incorrect.");
    }

    /**
     * Test for the getTopMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("getTopMostPos: devolve a linha mais acima")
    void testGetTopMostPos() {
        assertEquals(5, carrack.getTopMostPos(), "Error: The topmost position of the Carrack should be 5.");
    }

    /**
     * Test for the getBottomMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("getBottomMostPos: devolve a linha mais abaixo")
    void testGetBottomMostPos() {
        assertEquals(7, carrack.getBottomMostPos(), "Error: The bottommost position of the Carrack should be 7.");
    }

    /**
     * Test for the getLeftMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("getLeftMostPos: devolve a coluna mais à esquerda")
    void testGetLeftMostPos() {
        assertEquals(5, carrack.getLeftMostPos(), "Error: The leftmost position of the Carrack should be 5.");
    }

    /**
     * Test for the getRightMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("getRightMostPos: devolve a coluna mais à direita")
    void testGetRightMostPos() {
        assertEquals(5, carrack.getRightMostPos(), "Error: The rightmost position of the Carrack should be 5.");
    }

    /**
     * Test for the stillFloating method (all positions intact).
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("stillFloating: navio intacto continua a flutuar")
    void testStillFloating1() {
        assertTrue(carrack.stillFloating(), "Error: Carrack should initially be floating.");
    }

    /**
     * Test for the stillFloating method (some positions hit).
     */
    @Test
    @DisplayName("stillFloating: navio com uma posição atingida continua a flutuar")
    void testStillFloating2() {
        // Hit one position
        carrack.getPositions().get(0).shoot();
        assertTrue(carrack.stillFloating(), "Error: Carrack should still be floating when only one position is hit.");
    }

    /**
     * Test for the stillFloating method (all positions hit).
     */
    @Test
    @DisplayName("stillFloating: navio com todas as posições atingidas afunda")
    void testStillFloating3() {
        carrack.getPositions().forEach(position -> position.shoot());
        assertFalse(carrack.stillFloating(), "Error: Carrack should not be floating when all positions are hit.");
    }

    /**
     * Test for the constructor with null inputs.
     * Expect exception.
     */
    @Test
    @DisplayName("constructor: rumo e posição nulos lançam NullPointerException")
    void testConstructorWithNull() {
        assertThrows(NullPointerException.class, () -> new Carrack(null, null),
                "Error: Expected NullPointerException for null input.");
    }

    /**
     * Test for positions with EAST direction.
     */
    @Test
    @DisplayName("constructor: rumo EAST")
    void testConstructorDirectionEast() {
        Carrack carrackEast = new Carrack(Compass.EAST, new Position(5, 5));
        List<IPosition> positions = carrackEast.getPositions();

        assertNotNull(carrackEast, "Error: Carrack with EAST direction should not be null.");
        assertEquals(new Position(5, 5), positions.get(0), "Error: The first position is incorrect for EAST direction.");
        assertEquals(new Position(5, 6), positions.get(1), "Error: The second position is incorrect for EAST direction.");
        assertEquals(new Position(5, 7), positions.get(2), "Error: The third position is incorrect for EAST direction.");
    }

    /**
     * Test for positions with SOUTH direction.
     */
    @Test
    @DisplayName("constructor: rumo SOUTH")
    void testConstructorDirectionSouth() {
        Carrack carrackSouth = new Carrack(Compass.SOUTH, new Position(5, 5));
        List<IPosition> positions = carrackSouth.getPositions();

        assertNotNull(carrackSouth, "Error: Carrack with SOUTH direction should not be null.");
        assertEquals(new Position(5, 5), positions.get(0), "Error: The first position is incorrect for SOUTH direction.");
        assertEquals(new Position(6, 5), positions.get(1), "Error: The second position is incorrect for SOUTH direction.");
        assertEquals(new Position(7, 5), positions.get(2), "Error: The third position is incorrect for SOUTH direction.");
    }

    /**
     * Test for positions with WEST direction.
     */
    @Test
    @DisplayName("constructor: rumo WEST")
    void testConstructorDirectionWest() {
        Carrack carrackWest = new Carrack(Compass.WEST, new Position(5, 5));
        List<IPosition> positions = carrackWest.getPositions();

        assertNotNull(carrackWest, "Error: Carrack with WEST direction should not be null.");
        assertEquals(new Position(5, 5), positions.get(0), "Error: The first position is incorrect for WEST direction.");
        assertEquals(new Position(5, 6), positions.get(1), "Error: The second position is incorrect for WEST direction.");
        assertEquals(new Position(5, 7), positions.get(2), "Error: The third position is incorrect for WEST direction.");
    }

    /**
     * Test for the constructor's exception with invalid parameters.
     * Ensures assertions in the constructor work properly.
     */
    @Test
    @DisplayName("constructor: rumo ou posição nulos lançam NullPointerException")
    void testConstructorWithInvalidInput() {
        assertThrows(NullPointerException.class, () -> new Carrack(null, new Position(5, 5)),
                "Error: Should throw NullPointerException for null bearing.");
        assertThrows(NullPointerException.class, () -> new Carrack(Compass.NORTH, null),
                "Error: Should throw NullPointerException for null position.");
    }
}