package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import battleship.IPosition;

/**
 * Test class for Position.
 * Author: britoeabreu
 * Date: 2024-03-19 15:30
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - getRow: 1
 * - getColumn: 1
 * - isValid: 4
 * - isAdjacentTo: 4
 * - isOccupied: 1
 * - isHit: 1
 * - occupy: 1
 * - shoot: 1
 * - equals: 3
 * - hashCode: 1
 * - toString: 1
 */
public class PositionTest {
private Position position;

	@BeforeEach
	void setUp() {
		position = new Position(2, 3);
	//	position = new Position('C', 4);
	}

	@AfterEach
	void tearDown() {
		position = null;
	}

	@Test
	void constructor() {
		Position pos = new Position(1, 1);
		assertNotNull(pos, "Failed to create Position: object is null");
		assertEquals(1, pos.getRow(), "Failed to set row: expected 1 but got " + pos.getRow());
		assertEquals(1, pos.getColumn(), "Failed to set column: expected 1 but got " + pos.getColumn());
		assertFalse(pos.isOccupied(), "New position should not be occupied");
		assertFalse(pos.isHit(), "New position should not be hit");
	}

    @Test
    @DisplayName("Constructor(char,int): 'A',1 maps to (0,0)")
    void constructorCharInt_A1() {
        Position pos = new Position('A', 1);
        assertEquals(0, pos.getRow(),    "'A' should map to row 0");
        assertEquals(0, pos.getColumn(), "1 should map to column 0");
    }

    @Test
    @DisplayName("Constructor(char,int): 'C',4 maps to (2,3)")
    void constructorCharInt_C4() {
        Position pos = new Position('C', 4);
        assertEquals(2, pos.getRow(),    "'C' should map to row 2");
        assertEquals(3, pos.getColumn(), "4 should map to column 3");
    }

    @Test
    @DisplayName("Constructor(char,int): lowercase 'c' é tratado como 'C'")
    void constructorCharInt_lowercase() {
        assertEquals(new Position('C', 4).getRow(),
                new Position('c', 4).getRow(),
                "Lowercase char should yield same row as uppercase");
    }

	@Test
	void getRow() {
		assertEquals(2, position.getRow(), "Failed to get row: expected 2 but got " + position.getRow());
	}

	@Test
	void getColumn() {
		assertEquals(3, position.getColumn(), "Failed to get column: expected 3 but got " + position.getColumn());
	}

	@Test
	void getClassicRow() {
		assertEquals('C', position.getClassicRow(), "Failed to get row: expected 2 but got " + position.getRow());
	}

    @Test
    void getClassicColumn() {
        assertEquals(4, position.getClassicColumn(), "Failed to get column: expected 3 but got " + position.getColumn());
    }



	@Test
	void isValid1() {
		position = new Position(0, 0);
		assertTrue(position.isInside(), "Position (0,0) should be valid");
	}

	@Test
	void isValid2() {
		position = new Position(-1, 5);
		assertFalse(position.isInside(), "Position with negative row should be invalid");
	}

	@Test
	void isValid3() {
		position = new Position(5, -1);
		assertFalse(position.isInside(), "Position with negative column should be invalid");
	}

	@Test
	void isValid4() {
		position = new Position(Game.BOARD_SIZE, 5);
		assertFalse(position.isInside(), "Position with row >= BOARD_SIZE should be invalid");
	}

	@Test
	void isValid5() {
		position = new Position(5, Game.BOARD_SIZE);
		assertFalse(position.isInside(), "Position with column >= BOARD_SIZE should be invalid");
	}

	@Test
	void isAdjacentTo1() {
		Position other = new Position(2, 4);
		assertTrue(position.isAdjacentTo(other), "Failed to detect horizontally adjacent position");
	}

	@Test
	void isAdjacentTo2() {
		Position other = new Position(3, 3);
		assertTrue(position.isAdjacentTo(other), "Failed to detect vertically adjacent position");
	}

	@Test
	void isAdjacentTo3() {
		Position other = new Position(3, 4);
		assertTrue(position.isAdjacentTo(other), "Failed to detect diagonally adjacent position");
	}

	@Test
	void isAdjacentTo4() {
		Position other = new Position(4, 5);
		assertFalse(position.isAdjacentTo(other), "Non-adjacent position incorrectly identified as adjacent");
	}

	@Test
	void isAdjacentToWithNull() {
		assertThrows(NullPointerException.class, () -> position.isAdjacentTo(null),
				"isAdjacentTo should throw NullPointerException for null input");
	}

    @Test
    void adjacentPositions_interiorCell() {
        List<IPosition> adjacents = position.adjacentPositions(); // (2,3) → percorre todas as 8 direcções
        assertEquals(8, adjacents.size());
    }

    @Test
    void adjacentPositions_corner() {
        List<IPosition> adjacents = new Position(0, 0).adjacentPositions(); // canto → filtra inválidos
        assertEquals(3, adjacents.size());
    }

    @Test
    @DisplayName("adjacentPositions: aresta (0,3) tem exactamente 5 vizinhos")
    void adjacentPositions_edge() {
        assertEquals(5, new Position(0, 3).adjacentPositions().size(),
                "Top-edge cell must have exactly 5 valid adjacent positions");
    }

    @Test
    @DisplayName("adjacentPositions: todos os vizinhos estão dentro do tabuleiro")
    void adjacentPositions_allInsideBoard() {
        for (IPosition adj : position.adjacentPositions()) {
            assertTrue(((Position) adj).isInside(),
                    "Every adjacent position must be inside the board, but " + adj + " is not");
        }
    }

    @Test
    void isAdjacentTo_rowFarButColClose() {
        Position other = new Position(5, 3); // row muito distante, col igual
        assertFalse(position.isAdjacentTo(other));
    }






	@Test
	void isOccupied() {
		assertFalse(position.isOccupied(), "New position should not be occupied");
		position.occupy();
		assertTrue(position.isOccupied(), "Position should be occupied after occupy()");
	}

	@Test
	void isHit() {
		assertFalse(position.isHit(), "New position should not be hit");
		position.shoot();
		assertTrue(position.isHit(), "Position should be hit after shoot()");
	}

	@Test
	void equals1() {
		Position same = new Position(2, 3);
		assertTrue(position.equals(same), "Equal positions not identified as equal");
	}

	@Test
	void equals2() {
		assertFalse(position.equals(null), "Position should not equal null");
	}

	@Test
	void equals3() {
		Object other = new Object();
		assertFalse(position.equals(other), "Position should not equal non-Position object");
	}

	@Test
	void equals4() {
		Position other = new Position(2, 4);
		assertFalse(position.equals(other), "Positions with the same row but different column should not be equal");
	}

	@Test
	void equals5() {
		assertTrue(position.equals(position), "A position should be equal to itself");
	}

	@Test
	void hashCodeConsistency() {
		Position same = new Position(2, 3);
		assertEquals(position.hashCode(), same.hashCode(),
				"Hash codes not consistent for equal positions");
	}

    @Test
    @DisplayName("hashCode CONTRACT VIOLATION: equal positions with different state have different hashCodes")
    void hashCode_equalsContractViolation() {
        Position occupied = new Position(2, 3);
        occupied.occupy();

        assertTrue(position.equals(occupied),
                "equals() ignores isOccupied, so these should be equal");

        assertNotEquals(position.hashCode(), occupied.hashCode(), "hashCode includes isOccupied → contract with equals() is violated");
    }

    @Test
	void toStringFormat() {
//		String expected = "Row = C, Column = 4";
		String expected = "C4";
		assertEquals(expected, position.toString(),
				"Incorrect string representation: expected '" + expected +
						"' but got '" + position.toString() + "'");
	}

    @Test
    @DisplayName("randomPosition: nunca retorna null")
    void randomPosition_notNull() {
        assertNotNull(Position.randomPosition());
    }

    @Test
    @DisplayName("randomPosition: posição gerada está sempre dentro do tabuleiro")
    void randomPosition_isInsideBoard() {
        for (int i = 0; i < 50; i++) {
            Position rp = Position.randomPosition();
            assertTrue(rp.isInside(),
                    "randomPosition() must always be in-bounds, got " + rp);
        }
    }

    @Test
    void isAdjacentTo_rowCloseButColFar() {
        Position other = new Position(2, 7); // row diff=0 ✅, col diff=4 ❌
        assertFalse(position.isAdjacentTo(other));
    }



    @Test
    void equals_differentRow() {
        Position other = new Position(3, 3); // row diferente=3, coluna igual=3
        assertFalse(position.equals(other));
    }






}





