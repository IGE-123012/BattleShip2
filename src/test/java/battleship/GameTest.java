package battleship;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Game.
 * Cyclomatic Complexity covered per method:
 * - Constructor:              1
 * - fireSingleShot:           5 (invalid, repeated flag, repeated via moves, miss, hit, sink)
 * - fireShots:                3 (wrong size, duplicate in move, success + multiple moves)
 * - repeatedShot:             2 (found / not found)
 * - randomEnemyFire:          2 (enough positions / few positions)
 * - readEnemyFire:            3 (token "A3", token "A" + "3", incomplete, wrong count)
 * - getRemainingShips:        1
 * - getAlienFleet:            1 (documenta o bug conhecido)
 * - printBoard (static):      4 (show_shots, sunk ship adjacent marker, outside shot, legend)
 * - printMyBoard:             1
 * - printAlienBoard:          1
 * - jsonShots:                2 (lista normal, lista vazia)
 * - over:                     1
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

    @Test
    void constructor_myMovesInitiallyEmpty() {
        assertNotNull(game.getMyMoves());
        assertTrue(game.getMyMoves().isEmpty());
    }

    @Test
    void constructor_myFleetSetCorrectly() {
        assertSame(fleet, game.getMyFleet());
    }

    // ─── getAlienFleet — bug documentado ─────────────────────────────────────

    /**
     * Documenta o bug conhecido: getAlienFleet() devolve myFleet em vez da
     * alienFleet interna. O teste regista o comportamento atual para que qualquer
     * alteração futura seja detetada.
     */
    @Test
    void getAlienFleet_currentlyReturnsSameAsMyFleet() {
        // Bug: a implementação devolve myFleet em vez de alienFleet
        assertSame(game.getMyFleet(), game.getAlienFleet(),
                "Bug conhecido: getAlienFleet() devolve myFleet — alterar quando corrigido.");
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
        game.fireSingleShot(new Position(Game.BOARD_SIZE, 0), false);
        assertEquals(1, game.getInvalidShots());
    }

    @Test
    void fireSingleShot_invalidPosition_doesNotIncrementHits() {
        game.fireSingleShot(new Position(-1, 5), false);
        assertEquals(0, game.getHits());
    }

    // ─── fireSingleShot — tiro repetido (flag explícita) ─────────────────────

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

    // ─── fireSingleShot — tiro repetido (via alienMoves) ────────────────────

    /**
     * Verifica o ramo repeatedShot(pos) dentro de fireSingleShot quando isRepeated=false,
     * mas a posição já consta em alienMoves de um turno anterior.
     */
    @Test
    void fireSingleShot_positionAlreadyInAlienMoves_countedAsRepeated() {
        // Primeiro turno: dispara na posição (3,3)
        game.fireShots(List.of(new Position(3, 3), new Position(3, 4), new Position(3, 5)));

        // Segundo disparo isolado na mesma posição sem flag — deve detetar repetição
        game.fireSingleShot(new Position(3, 3), false);
        assertEquals(1, game.getRepeatedShots());
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
    void fireSingleShot_hitWithoutSinking_incrementsHitsOnly() {
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
        game.fireShots(List.of(
                new Position(2, 3),
                new Position(2, 4),
                new Position(2, 5)));
        assertTrue(game.repeatedShot(new Position(2, 3)));
    }

    @Test
    void repeatedShot_positionNotFired_returnsFalse() {
        game.fireShots(List.of(
                new Position(2, 3),
                new Position(2, 4),
                new Position(2, 5)));
        assertFalse(game.repeatedShot(new Position(9, 9)));
    }

    // ─── fireShots ────────────────────────────────────────────────────────────

    @Test
    void fireShots_addsToAlienMoves() {
        game.fireShots(List.of(
                new Position(0, 0),
                new Position(1, 1),
                new Position(2, 2)));
        assertEquals(1, game.getAlienMoves().size());
    }

    @Test
    void fireShots_wrongNumberOfShots_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> game.fireShots(List.of(new Position(0, 0))));
    }

    @Test
    void fireShots_duplicatePositionsInSameMove_incrementsRepeatedShots() {
        game.fireShots(List.of(
                new Position(0, 0),
                new Position(0, 0),
                new Position(1, 1)));
        assertEquals(1, game.getRepeatedShots());
    }

    /**
     * Dois turnos consecutivos: verifica que alienMoves cresce e que o
     * moveNumber avança (o segundo move não pode ser acedido via API pública,
     * mas o tamanho da lista é suficiente para confirmar o incremento).
     */
    @Test
    void fireShots_multipleTurns_alienMovesGrows() {
        game.fireShots(List.of(
                new Position(0, 0), new Position(0, 1), new Position(0, 2)));
        game.fireShots(List.of(
                new Position(1, 0), new Position(1, 1), new Position(1, 2)));
        assertEquals(2, game.getAlienMoves().size());
    }

    /**
     * Após dois turnos, repeatedShot deve detetar posições de ambos os turnos.
     */
    @Test
    void fireShots_multipleTurns_repeatedShotSpansBothTurns() {
        game.fireShots(List.of(
                new Position(0, 0), new Position(0, 1), new Position(0, 2)));
        game.fireShots(List.of(
                new Position(1, 0), new Position(1, 1), new Position(1, 2)));
        assertTrue(game.repeatedShot(new Position(0, 0)));
        assertTrue(game.repeatedShot(new Position(1, 2)));
        assertFalse(game.repeatedShot(new Position(9, 9)));
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
        fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
        Ship frigate = new Frigate(Compass.EAST, new Position(5, 5));
        fleet.addShip(frigate);
        frigate.sink();
        assertEquals(1, game.getRemainingShips());
    }

    // ─── randomEnemyFire ──────────────────────────────────────────────────────

    /**
     * Verifica que randomEnemyFire() devolve JSON válido e regista um move.
     */
    @Test
    void randomEnemyFire_returnsValidJson() {
        String json = game.randomEnemyFire();
        assertNotNull(json);
        assertTrue(json.contains("row"));
        assertTrue(json.contains("column"));
    }

    @Test
    void randomEnemyFire_addsToAlienMoves() {
        game.randomEnemyFire();
        assertEquals(1, game.getAlienMoves().size());
    }

    /**
     * Testa o ramo "poucos candidatos" preenchendo quase todo o tabuleiro
     * com moves anteriores, deixando menos posições disponíveis do que NUMBER_SHOTS.
     * Não é prático esgotar todas as posições em teste unitário, mas podemos verificar
     * que o método não lança exceção mesmo com muitos tiros anteriores.
     */
    @Test
    void randomEnemyFire_afterManyMoves_doesNotThrow() {
        // Dispara 30 turnos (90 posições distintas) para reduzir o espaço disponível
        for (int turn = 0; turn < 30; turn++) {
            int base = turn * 3;
            int r0 = base / Game.BOARD_SIZE, c0 = base % Game.BOARD_SIZE;
            int r1 = (base + 1) / Game.BOARD_SIZE, c1 = (base + 1) % Game.BOARD_SIZE;
            int r2 = (base + 2) / Game.BOARD_SIZE, c2 = (base + 2) % Game.BOARD_SIZE;
            game.fireShots(List.of(
                    new Position(r0, c0),
                    new Position(r1, c1),
                    new Position(r2, c2)));
        }
        assertDoesNotThrow(() -> game.randomEnemyFire());
    }

    // ─── readEnemyFire ────────────────────────────────────────────────────────

    /**
     * Formato compacto "A1 B2 C3": três tokens combinados coluna+linha.
     */
    @Test
    void readEnemyFire_compactFormat_parsesCorrectly() {
        Scanner in = new Scanner("A1 B2 C3\n");
        String json = game.readEnemyFire(in);
        assertNotNull(json);
        assertTrue(json.contains("row"));
        assertEquals(1, game.getAlienMoves().size());
    }

    /**
     * Formato separado "A 1 B 2 C 3": coluna e linha como tokens distintos.
     */
    @Test
    void readEnemyFire_separatedFormat_parsesCorrectly() {
        Scanner in = new Scanner("A 1 B 2 C 3\n");
        String json = game.readEnemyFire(in);
        assertNotNull(json);
        assertEquals(1, game.getAlienMoves().size());
    }

    /**
     * Mistura de formatos compacto e separado na mesma linha.
     */
    @Test
    void readEnemyFire_mixedFormat_parsesCorrectly() {
        Scanner in = new Scanner("A1 B 2 C3\n");
        assertDoesNotThrow(() -> game.readEnemyFire(in));
    }

    /**
     * Coluna sem linha a seguir: deve lançar IllegalArgumentException.
     */
    @Test
    void readEnemyFire_incompletePosition_throwsException() {
        // "A" isolado, sem número a seguir
        Scanner in = new Scanner("A B C\n");
        assertThrows(IllegalArgumentException.class, () -> game.readEnemyFire(in));
    }

    /**
     * Número insuficiente de posições: deve lançar IllegalArgumentException.
     */
    @Test
    void readEnemyFire_tooFewPositions_throwsException() {
        Scanner in = new Scanner("A1 B2\n");
        assertThrows(IllegalArgumentException.class, () -> game.readEnemyFire(in));
    }

    // ─── printBoard (static) ─────────────────────────────────────────────────

    @Test
    void printBoard_showShotsAndLegend_doesNotThrow() {
        fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0)));
        game.fireShots(List.of(
                new Position(0, 0),
                new Position(1, 1),
                new Position(2, 2)));
        assertDoesNotThrow(() -> Game.printBoard(fleet, game.getAlienMoves(), true, true));
    }

    @Test
    void printBoard_emptyFleetAndMoves_doesNotThrow() {
        assertDoesNotThrow(() -> Game.printBoard(fleet, List.of(), false, false));
    }

    /**
     * Verifica o ramo SHIP_ADJACENT_MARKER: afundar um navio faz printBoard
     * marcar as posições adjacentes com o marcador '-'.
     */
    @Test
    void printBoard_sunkShip_showsAdjacentMarker() {
        Barge barge = new Barge(Compass.NORTH, new Position(5, 5));
        fleet.addShip(barge);
        // Afundar o navio via tiro
        game.fireShots(List.of(
                new Position(5, 5),
                new Position(0, 0),
                new Position(0, 1)));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        Game.printBoard(fleet, game.getAlienMoves(), false, false);
        System.setOut(System.out);

        assertTrue(out.toString().contains("-"),
                "Board deve mostrar marcador adjacente '-' após afundar navio.");
    }

    /**
     * Verifica o ramo show_shots=true com tiro na água (SHOT_WATER_MARKER 'o').
     */
    @Test
    void printBoard_showShots_waterShotMarkerPresent() {
        game.fireShots(List.of(
                new Position(5, 5),
                new Position(5, 6),
                new Position(5, 7)));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        Game.printBoard(fleet, game.getAlienMoves(), true, false);
        System.setOut(System.out);

        assertTrue(out.toString().contains("o"),
                "Board deve mostrar marcador 'o' para tiros na água.");
    }

    /**
     * Verifica o ramo show_shots=true com tiro certeiro (SHOT_SHIP_MARKER '*').
     */
    @Test
    void printBoard_showShots_hitMarkerPresent() {
        fleet.addShip(new Barge(Compass.NORTH, new Position(3, 3)));
        game.fireShots(List.of(
                new Position(3, 3),
                new Position(0, 0),
                new Position(0, 1)));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        Game.printBoard(fleet, game.getAlienMoves(), true, false);
        System.setOut(System.out);

        assertTrue(out.toString().contains("*"),
                "Board deve mostrar marcador '*' para tiro certeiro.");
    }

    /**
     * Verifica que show_shots=false não exibe marcadores de tiro.
     */
    @Test
    void printBoard_hiddenShots_noShotMarkersInOutput() {
        fleet.addShip(new Barge(Compass.NORTH, new Position(3, 3)));
        game.fireShots(List.of(
                new Position(3, 3),
                new Position(0, 0),
                new Position(0, 1)));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        Game.printBoard(fleet, game.getAlienMoves(), false, false);
        System.setOut(System.out);

        assertFalse(out.toString().contains("*"),
                "Marcador '*' não deve aparecer quando show_shots=false.");
        assertFalse(out.toString().contains("o"),
                "Marcador 'o' não deve aparecer quando show_shots=false.");
    }

    /**
     * Verifica que a legenda aparece quando showLegend=true.
     */
    @Test
    void printBoard_legendShown_containsLegendText() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        Game.printBoard(fleet, List.of(), false, true);
        System.setOut(System.out);

        assertTrue(out.toString().contains("LEGENDA"),
                "Output deve conter 'LEGENDA' quando showLegend=true.");
    }

    /**
     * Verifica que a legenda não aparece quando showLegend=false.
     */
    @Test
    void printBoard_legendHidden_doesNotContainLegendText() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        Game.printBoard(fleet, List.of(), false, false);
        System.setOut(System.out);

        assertFalse(out.toString().contains("LEGENDA"),
                "Output não deve conter 'LEGENDA' quando showLegend=false.");
    }

    // ─── printMyBoard / printAlienBoard (smoke tests) ────────────────────────

    @Test
    void printMyBoard_withShotsAndLegend_doesNotThrow() {
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

    /**
     * Verifica que o JSON gerado contém as coordenadas clássicas corretas.
     */
    @Test
    void jsonShots_singleShot_containsExpectedRowAndColumn() {
        // Position(0,0) => linha clássica 'A', coluna clássica 1
        List<IPosition> shots = List.of(new Position(0, 0));
        String json = Game.jsonShots(shots);
        assertTrue(json.contains("\"A\"") || json.contains("A"),
                "JSON deve conter a linha clássica 'A' para row=0.");
        assertTrue(json.contains("1"),
                "JSON deve conter a coluna clássica '1' para col=0.");
    }

    /**
     * Verifica que o número de entradas no JSON corresponde ao número de tiros.
     */
    @Test
    void jsonShots_multipleShots_correctEntryCount() {
        List<IPosition> shots = List.of(
                new Position(0, 0),
                new Position(1, 1),
                new Position(2, 2));
        String json = Game.jsonShots(shots);
        // Cada entry tem "row" — contar ocorrências como proxy do número de objectos
        long count = json.chars()
                .filter(c -> c == '{')
                .count();
        assertEquals(3, count, "JSON deve conter 3 objectos para 3 tiros.");
    }

    // ─── over ─────────────────────────────────────────────────────────────────

    @Test
    void over_doesNotThrow() {
        assertDoesNotThrow(() -> game.over());
    }

    @Test
    void over_outputContainsFarewellMessage() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        game.over();
        System.setOut(System.out);

        assertTrue(out.toString().contains("Java Sparrow"),
                "Mensagem de fim de jogo deve conter 'Java Sparrow'.");
    }
}