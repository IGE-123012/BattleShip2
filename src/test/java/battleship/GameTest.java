package battleship;

import java.util.List;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Game.
 * Author: britoeabreu
 * Date: 2024-03-19
 */
public class GameTest {

    private Game game;
    private Fleet fleet;

    @BeforeEach
    void setUp() {
        fleet = new Fleet();
        game = new Game(fleet);
    }

    @AfterEach
    void tearDown() {
        game = null;
        fleet = null;
    }

    // ─── Constructor ──────────────────────────────────────────────────────────

    @Test
    void constructor_initialStateIsEmpty() {
        assertNotNull(game);
        assertNotNull(game.getAlienMoves());
        assertTrue(game.getAlienMoves().isEmpty());
        assertEquals(0, game.getInvalidShots());
        assertEquals(0, game.getRepeatedShots());
        assertEquals(0, game.getHits());
        assertEquals(0, game.getSunkShips());
    }

    // ─── fireSingleShot — posição inválida ───────────────────────────────────

    @Test
    void fireSingleShot_invalidPosition_negativeRow_incrementsInvalidShots() {
        game.fireSingleShot(new Position(-1, 5), false);
        assertEquals(1, game.getInvalidShots());
    }

    @Test
    void fireSingleShot_invalidPosition_negativeCol_incrementsInvalidShots() {
        game.fireSingleShot(new Position(5, -1), false);
        assertEquals(1, game.getInvalidShots());
    }

    @Test
    void fireSingleShot_invalidPosition_rowOverflow_incrementsInvalidShots() {
        game.fireSingleShot(new Position(10, 0), false);
        assertEquals(1, game.getInvalidShots());
    }

    @Test
    void fireSingleShot_invalidPosition_doesNotAddToAlienMoves() {
        game.fireSingleShot(new Position(-1, 5), false);
        assertTrue(game.getAlienMoves().isEmpty());
    }

    // ─── fireSingleShot — tiro repetido ──────────────────────────────────────

    @Test
    void fireSingleShot_repeatedFlag_incrementsRepeatedShots() {
        game.fireSingleShot(new Position(2, 3), true);
        assertEquals(1, game.getRepeatedShots());
    }

    @Test
    void fireSingleShot_repeatedFlag_doesNotIncrementHits() {
        game.fireSingleShot(new Position(2, 3), true);
        assertEquals(0, game.getHits());
    }

    // ─── fireSingleShot — miss ───────────────────────────────────────────────

    @Test
    void fireSingleShot_miss_doesNotIncrementHits() {
        game.fireSingleShot(new Position(5, 5), false);
        assertEquals(0, game.getHits());
    }

    @Test
    void fireSingleShot_miss_doesNotIncrementSunkShips() {
        game.fireSingleShot(new Position(5, 5), false);
        assertEquals(0, game.getSunkShips());
    }

    // ─── fireSingleShot — hit sem afundar ────────────────────────────────────

    @Test
    void fireSingleShot_hitWithoutSinking_incrementsHits() {
        // Frigate ocupa 2 células; acertar numa não a afunda
        fleet.addShip(new Frigate(Compass.NORTH, new Position(3, 3)));
        game.fireSingleShot(new Position(3, 3), false);
        assertEquals(1, game.getHits());
        assertEquals(0, game.getSunkShips());
    }

    // ─── fireSingleShot — hit e afundar ──────────────────────────────────────

    @Test
    void fireSingleShot_hitAndSinkBarge_incrementsHitsAndSunkShips() {
        // Barge ocupa 1 célula; um tiro afunda-a
        fleet.addShip(new Barge(Compass.NORTH, new Position(2, 2)));
        game.fireSingleShot(new Position(2, 2), false);
        assertEquals(1, game.getHits());
        assertEquals(1, game.getSunkShips());
    }

    @Test
    void fireSingleShot_sinkAllShips_remainingShipsIsZero() {
        fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0)));
        fleet.addShip(new Barge(Compass.NORTH, new Position(5, 5)));
        game.fireSingleShot(new Position(0, 0), false);
        game.fireSingleShot(new Position(5, 5), false);
        assertEquals(0, game.getRemainingShips());
        assertEquals(2, game.getSunkShips());
    }

    // ─── repeatedShot ─────────────────────────────────────────────────────────

    @Test
    void repeatedShot_beforeFiring_returnsFalse() {
        assertFalse(game.repeatedShot(new Position(2, 3)));
    }

    @Test
    void repeatedShot_afterFireShots_returnsTrue() {
        List<IPosition> shots = List.of(
                new Position(2, 3),
                new Position(2, 4),
                new Position(2, 5)
        );
        game.fireShots(shots);
        assertTrue(game.repeatedShot(new Position(2, 3)));
    }

    @Test
    void repeatedShot_positionNotFired_returnsFalse() {
        List<IPosition> shots = List.of(
                new Position(2, 3),
                new Position(2, 4),
                new Position(2, 5)
        );
        game.fireShots(shots);
        assertFalse(game.repeatedShot(new Position(9, 9)));
    }

    // ─── fireShots ────────────────────────────────────────────────────────────

    @Test
    void fireShots_addsToAlienMoves() {
        List<IPosition> shots = List.of(
                new Position(0, 0),
                new Position(1, 1),
                new Position(2, 2)
        );
        game.fireShots(shots);
        assertEquals(1, game.getAlienMoves().size());
    }

    @Test
    void fireShots_wrongNumberOfShots_throwsException() {
        List<IPosition> shots = List.of(new Position(0, 0));
        assertThrows(IllegalArgumentException.class, () -> game.fireShots(shots));
    }

    @Test
    void fireShots_duplicatePositionsInSameMove_incrementsRepeatedShots() {
        List<IPosition> shots = List.of(
                new Position(0, 0),
                new Position(0, 0),
                new Position(1, 1)
        );
        game.fireShots(shots);
        assertEquals(1, game.getRepeatedShots());
    }

    // ─── getRemainingShips ────────────────────────────────────────────────────

    @Test
    void getRemainingShips_emptyFleet_returnsZero() {
        assertEquals(0, game.getRemainingShips());
    }

    @Test
    void getRemainingShips_afterAddingShips_returnsCorrectCount() {
        fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
        fleet.addShip(new Frigate(Compass.EAST, new Position(5, 5)));
        assertEquals(2, game.getRemainingShips());
    }

    @Test
    void getRemainingShips_afterSinkingOne_decrementsCorrectly() {
        Ship barge = new Barge(Compass.NORTH, new Position(1, 1));
        Ship frigate = new Frigate(Compass.EAST, new Position(5, 5));
        fleet.addShip(barge);
        fleet.addShip(frigate);
        frigate.sink();
        assertEquals(1, game.getRemainingShips());
    }

    // ─── getMyFleet ───────────────────────────────────────────────────────────

    @Test
    void getMyFleet_returnsSameFleetPassedToConstructor() {
        assertSame(fleet, game.getMyFleet());
    }

    // ─── printMyBoard / printAlienBoard (smoke tests) ────────────────────────

    @Test
    void printMyBoard_doesNotThrow() {
        fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0)));
        assertDoesNotThrow(() -> game.printMyBoard(true, true));
    }

    @Test
    void printMyBoard_withoutShotsAndLegend_doesNotThrow() {
        assertDoesNotThrow(() -> game.printMyBoard(false, false));
    }

    @Test
    void printAlienBoard_doesNotThrow() {
        assertDoesNotThrow(() -> game.printAlienBoard(false, false));
    }

    // ─── printBoard (static) ─────────────────────────────────────────────────

    @Test
    void printBoard_static_withShowShotsAndLegend_doesNotThrow() {
        fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0)));
        List<IPosition> shots = List.of(
                new Position(0, 0),
                new Position(1, 1),
                new Position(2, 2)
        );
        game.fireShots(shots);
        assertDoesNotThrow(() -> Game.printBoard(fleet, game.getAlienMoves(), true, true));
    }

    @Test
    void printBoard_static_emptyFleetAndMoves_doesNotThrow() {
        assertDoesNotThrow(() -> Game.printBoard(fleet, List.of(), false, false));
    }

    // ─── jsonShots (static) ───────────────────────────────────────────────────

    @Test
    void jsonShots_returnsValidJson() {
        List<IPosition> shots = List.of(new Position(0, 0), new Position(1, 1));
        String json = Game.jsonShots(shots);
        assertNotNull(json);
        assertTrue(json.contains("row"));
        assertTrue(json.contains("column"));
    }

    @Test
    void jsonShots_emptyList_returnsEmptyJsonArray() {
        String json = Game.jsonShots(List.of());
        assertNotNull(json);
        assertTrue(json.trim().startsWith("["));
    }
}