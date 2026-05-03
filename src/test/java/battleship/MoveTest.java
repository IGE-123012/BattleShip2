package battleship;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Test class for Move.
 * Cyclomatic Complexity covered per method:
 * - Constructor:              1
 * - getNumber:                1
 * - getShots:                 1
 * - getShotResults:           1
 * - toString:                 1
 * - processEnemyFire:        12 (tiro inválido, repetido, na água, hit sem afundar,
 *                                hit e afundar, múltiplos afundados, múltiplos hits,
 *                                verbose=true, verbose=false, tiros fora do tabuleiro,
 *                                só repetidos, mix repetidos + válidos)
 */
public class MoveTest {

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Resultado: tiro inválido (fora do tabuleiro). */
    private static IGame.ShotResult invalid() {
        return new IGame.ShotResult(false, false, null, false);
    }

    /** Resultado: tiro repetido. */
    private static IGame.ShotResult repeated() {
        return new IGame.ShotResult(true, true, null, false);
    }

    /** Resultado: tiro na água (válido, sem acertar). */
    private static IGame.ShotResult miss() {
        return new IGame.ShotResult(true, false, null, false);
    }

    /** Resultado: tiro num navio sem o afundar. */
    private static IGame.ShotResult hit(IShip ship) {
        return new IGame.ShotResult(true, false, ship, false);
    }

    /** Resultado: tiro que afunda um navio. */
    private static IGame.ShotResult sink(IShip ship) {
        return new IGame.ShotResult(true, false, ship, true);
    }

    /** Lê o JSON devolvido por processEnemyFire como JsonNode para asserções. */
    private static JsonNode parseJson(String json) throws Exception {
        return new ObjectMapper().readTree(json);
    }

    /** Posições genéricas para construir um Move (conteúdo irrelevante nos testes de processEnemyFire). */
    private static List<IPosition> threePositions() {
        return List.of(new Position(0, 0), new Position(1, 1), new Position(2, 2));
    }

    // ─── Constructor ──────────────────────────────────────────────────────────

    @Test
    void constructor_storesAllFields() {
        List<IPosition> shots = threePositions();
        List<IGame.ShotResult> results = List.of(miss(), miss(), miss());

        Move move = new Move(5, shots, results);

        assertEquals(5, move.getNumber());
        assertSame(shots, move.getShots());
        assertSame(results, move.getShotResults());
    }

    // ─── getNumber ────────────────────────────────────────────────────────────

    @Test
    void getNumber_returnsValuePassedToConstructor() {
        Move move = new Move(3, threePositions(), List.of(miss(), miss(), miss()));
        assertEquals(3, move.getNumber());
    }

    @Test
    void getNumber_moveNumberOne() {
        Move move = new Move(1, threePositions(), List.of(miss(), miss(), miss()));
        assertEquals(1, move.getNumber());
    }

    // ─── getShots ─────────────────────────────────────────────────────────────

    @Test
    void getShots_returnsSameListPassedToConstructor() {
        List<IPosition> shots = threePositions();
        Move move = new Move(1, shots, List.of(miss(), miss(), miss()));
        assertSame(shots, move.getShots());
    }

    @Test
    void getShots_hasSizeThree() {
        Move move = new Move(1, threePositions(), List.of(miss(), miss(), miss()));
        assertEquals(3, move.getShots().size());
    }

    // ─── getShotResults ───────────────────────────────────────────────────────

    @Test
    void getShotResults_returnsSameListPassedToConstructor() {
        List<IGame.ShotResult> results = List.of(miss(), miss(), miss());
        Move move = new Move(1, threePositions(), results);
        assertSame(results, move.getShotResults());
    }

    // ─── toString ─────────────────────────────────────────────────────────────

    @Test
    void toString_containsMoveNumber() {
        Move move = new Move(7, threePositions(), List.of(miss(), miss(), miss()));
        assertTrue(move.toString().contains("7"));
    }

    @Test
    void toString_containsShotsCount() {
        Move move = new Move(1, threePositions(), List.of(miss(), miss(), miss()));
        assertTrue(move.toString().contains("3"));
    }

    @Test
    void toString_isNotNull() {
        Move move = new Move(1, threePositions(), List.of(miss(), miss(), miss()));
        assertNotNull(move.toString());
    }

    // ─── processEnemyFire — JSON estrutura base ───────────────────────────────

    @Test
    void processEnemyFire_returnsNonNullJson() {
        Move move = new Move(1, threePositions(), List.of(miss(), miss(), miss()));
        assertNotNull(move.processEnemyFire(false));
    }

    @Test
    void processEnemyFire_returnedJsonIsValidJson() throws Exception {
        Move move = new Move(1, threePositions(), List.of(miss(), miss(), miss()));
        String json = move.processEnemyFire(false);
        assertDoesNotThrow(() -> new ObjectMapper().readTree(json));
    }

    @Test
    void processEnemyFire_jsonContainsExpectedKeys() throws Exception {
        Move move = new Move(1, threePositions(), List.of(miss(), miss(), miss()));
        JsonNode root = parseJson(move.processEnemyFire(false));

        assertTrue(root.has("validShots"),    "JSON deve conter 'validShots'.");
        assertTrue(root.has("repeatedShots"), "JSON deve conter 'repeatedShots'.");
        assertTrue(root.has("missedShots"),   "JSON deve conter 'missedShots'.");
        assertTrue(root.has("outsideShots"),  "JSON deve conter 'outsideShots'.");
        assertTrue(root.has("sunkBoats"),     "JSON deve conter 'sunkBoats'.");
        assertTrue(root.has("hitsOnBoats"),   "JSON deve conter 'hitsOnBoats'.");
    }

    // ─── processEnemyFire — todos os tiros na água ───────────────────────────

    @Test
    void processEnemyFire_allMisses_validShotsIsThree() throws Exception {
        Move move = new Move(1, threePositions(), List.of(miss(), miss(), miss()));
        JsonNode root = parseJson(move.processEnemyFire(false));

        assertEquals(3, root.get("validShots").asInt());
        assertEquals(3, root.get("missedShots").asInt());
        assertEquals(0, root.get("repeatedShots").asInt());
        assertEquals(0, root.get("outsideShots").asInt());
    }

    // ─── processEnemyFire — tiros inválidos (fora do tabuleiro) ─────────────

    @Test
    void processEnemyFire_allInvalid_outsideShotsIsThree() throws Exception {
        Move move = new Move(1, threePositions(), List.of(invalid(), invalid(), invalid()));
        JsonNode root = parseJson(move.processEnemyFire(false));

        assertEquals(0, root.get("validShots").asInt());
        assertEquals(3, root.get("outsideShots").asInt());
        assertEquals(0, root.get("repeatedShots").asInt());
    }

    @Test
    void processEnemyFire_mixInvalidAndMiss_outsideShotsCorrect() throws Exception {
        // 1 inválido + 2 na água => outsideShots=1, validShots=2
        Move move = new Move(1, threePositions(), List.of(invalid(), miss(), miss()));
        JsonNode root = parseJson(move.processEnemyFire(false));

        assertEquals(1, root.get("outsideShots").asInt());
        assertEquals(2, root.get("validShots").asInt());
    }

    // ─── processEnemyFire — tiros repetidos ──────────────────────────────────

    @Test
    void processEnemyFire_allRepeated_repeatedShotsIsThree() throws Exception {
        Move move = new Move(1, threePositions(), List.of(repeated(), repeated(), repeated()));
        JsonNode root = parseJson(move.processEnemyFire(false));

        assertEquals(3, root.get("repeatedShots").asInt());
        assertEquals(0, root.get("validShots").asInt());
        assertEquals(0, root.get("outsideShots").asInt());
    }

    @Test
    void processEnemyFire_mixRepeatedAndMiss_countsCorrect() throws Exception {
        // 1 repetido + 2 na água
        Move move = new Move(1, threePositions(), List.of(repeated(), miss(), miss()));
        JsonNode root = parseJson(move.processEnemyFire(false));

        assertEquals(1, root.get("repeatedShots").asInt());
        assertEquals(2, root.get("validShots").asInt());
        assertEquals(2, root.get("missedShots").asInt());
    }

    // ─── processEnemyFire — hit sem afundar ──────────────────────────────────

    @Test
    void processEnemyFire_hitWithoutSinking_hitsOnBoatsNotEmpty() throws Exception {
        IShip frigate = new Frigate(Compass.NORTH, new Position(3, 3));
        // hit na fragata (2 células, 1 tiro não a afunda)
        Move move = new Move(1, threePositions(), List.of(hit(frigate), miss(), miss()));
        JsonNode root = parseJson(move.processEnemyFire(false));

        assertEquals(0, root.get("sunkBoats").size());
        assertEquals(1, root.get("hitsOnBoats").size());
        assertEquals(1, root.get("validShots").asInt());
        assertEquals(2, root.get("missedShots").asInt());
    }

    @Test
    void processEnemyFire_hitWithoutSinking_correctBoatType() throws Exception {
        IShip frigate = new Frigate(Compass.NORTH, new Position(3, 3));
        Move move = new Move(1, threePositions(), List.of(hit(frigate), miss(), miss()));
        JsonNode root = parseJson(move.processEnemyFire(false));

        String type = root.get("hitsOnBoats").get(0).get("type").asText();
        assertEquals(frigate.getCategory(), type);
    }

    // ─── processEnemyFire — hit e afundar ────────────────────────────────────

    @Test
    void processEnemyFire_sinkBarge_sunkBoatsCountOne() throws Exception {
        IShip barge = new Barge(Compass.NORTH, new Position(2, 2));
        Move move = new Move(1, threePositions(), List.of(sink(barge), miss(), miss()));
        JsonNode root = parseJson(move.processEnemyFire(false));

        assertEquals(1, root.get("sunkBoats").size());
        assertEquals(0, root.get("hitsOnBoats").size());
    }

    @Test
    void processEnemyFire_sinkBarge_correctTypeInSunkBoats() throws Exception {
        IShip barge = new Barge(Compass.NORTH, new Position(2, 2));
        Move move = new Move(1, threePositions(), List.of(sink(barge), miss(), miss()));
        JsonNode root = parseJson(move.processEnemyFire(false));

        String type = root.get("sunkBoats").get(0).get("type").asText();
        assertEquals(barge.getCategory(), type);
    }

    @Test
    void processEnemyFire_sinkBarge_countIsOne() throws Exception {
        IShip barge = new Barge(Compass.NORTH, new Position(2, 2));
        Move move = new Move(1, threePositions(), List.of(sink(barge), miss(), miss()));
        JsonNode root = parseJson(move.processEnemyFire(false));

        int count = root.get("sunkBoats").get(0).get("count").asInt();
        assertEquals(1, count);
    }

    /**
     * Navio afundado NÃO deve aparecer em hitsOnBoats.
     */
    @Test
    void processEnemyFire_sunkShip_notInHitsOnBoats() throws Exception {
        IShip barge = new Barge(Compass.NORTH, new Position(2, 2));
        Move move = new Move(1, threePositions(), List.of(sink(barge), miss(), miss()));
        JsonNode root = parseJson(move.processEnemyFire(false));

        assertTrue(root.get("hitsOnBoats").isEmpty(),
                "Navio afundado não deve aparecer em hitsOnBoats.");
    }

    // ─── processEnemyFire — múltiplos afundados do mesmo tipo ────────────────

    @Test
    void processEnemyFire_twoBargesSunk_countIsTwo() throws Exception {
        IShip b1 = new Barge(Compass.NORTH, new Position(0, 0));
        IShip b2 = new Barge(Compass.NORTH, new Position(5, 5));
        Move move = new Move(1, threePositions(), List.of(sink(b1), sink(b2), miss()));
        JsonNode root = parseJson(move.processEnemyFire(false));

        JsonNode sunk = root.get("sunkBoats");
        assertEquals(1, sunk.size(), "Deve haver 1 entrada para o tipo 'Barca'.");
        assertEquals(2, sunk.get(0).get("count").asInt(),
                "Devem estar 2 barcas afundadas.");
    }

    // ─── processEnemyFire — cenário misto completo ───────────────────────────

    @Test
    void processEnemyFire_mixAllResultTypes_allCountersCorrect() throws Exception {
        // 1 inválido + 1 repetido + 1 hit numa fragata => outsideShots=1, repeated=1, valid=1
        IShip frigate = new Frigate(Compass.NORTH, new Position(3, 3));
        Move move = new Move(2, threePositions(), List.of(invalid(), repeated(), hit(frigate)));
        JsonNode root = parseJson(move.processEnemyFire(false));

        assertEquals(1, root.get("outsideShots").asInt());
        assertEquals(1, root.get("repeatedShots").asInt());
        assertEquals(1, root.get("validShots").asInt());
        assertEquals(0, root.get("missedShots").asInt());
        assertEquals(0, root.get("sunkBoats").size());
        assertEquals(1, root.get("hitsOnBoats").size());
    }

    // ─── processEnemyFire — verbose=true ─────────────────────────────────────

    @Test
    void processEnemyFire_verboseTrue_doesNotThrow() {
        Move move = new Move(1, threePositions(), List.of(miss(), miss(), miss()));
        assertDoesNotThrow(() -> move.processEnemyFire(true));
    }

    @Test
    void processEnemyFire_verboseTrue_outputContainsMoveNumber() {
        Move move = new Move(4, threePositions(), List.of(miss(), miss(), miss()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        move.processEnemyFire(true);
        System.setOut(System.out);

        assertTrue(out.toString().contains("4"),
                "Output verbose deve conter o número da jogada.");
    }

    @Test
    void processEnemyFire_verboseTrue_allMisses_outputContainsAgua() {
        Move move = new Move(1, threePositions(), List.of(miss(), miss(), miss()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        move.processEnemyFire(true);
        System.setOut(System.out);

        assertTrue(out.toString().contains("água"),
                "Output verbose deve mencionar tiros na água.");
    }

    @Test
    void processEnemyFire_verboseTrue_allRepeated_outputContainsRepetido() {
        Move move = new Move(1, threePositions(), List.of(repeated(), repeated(), repeated()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        move.processEnemyFire(true);
        System.setOut(System.out);

        assertTrue(out.toString().contains("repetido"),
                "Output verbose deve mencionar tiros repetidos.");
    }

    @Test
    void processEnemyFire_verboseTrue_sunkShip_outputContainsFundo() {
        IShip barge = new Barge(Compass.NORTH, new Position(2, 2));
        Move move = new Move(1, threePositions(), List.of(sink(barge), miss(), miss()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        move.processEnemyFire(true);
        System.setOut(System.out);

        assertTrue(out.toString().contains("fundo"),
                "Output verbose deve mencionar navio ao fundo.");
    }

    @Test
    void processEnemyFire_verboseTrue_invalidShots_outputContainsExterior() {
        Move move = new Move(1, threePositions(), List.of(invalid(), invalid(), miss()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        move.processEnemyFire(true);
        System.setOut(System.out);

        assertTrue(out.toString().contains("exterior"),
                "Output verbose deve mencionar tiros exteriores.");
    }

    @Test
    void processEnemyFire_verboseTrue_hitWithoutSinking_outputContainsNum() {
        IShip frigate = new Frigate(Compass.NORTH, new Position(3, 3));
        Move move = new Move(1, threePositions(), List.of(hit(frigate), miss(), miss()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        move.processEnemyFire(true);
        System.setOut(System.out);

        assertTrue(out.toString().contains("num"),
                "Output verbose deve mencionar tiro 'num(a)' navio.");
    }

    @Test
    void processEnemyFire_verboseFalse_noOutputToConsole() {
        Move move = new Move(1, threePositions(), List.of(miss(), miss(), miss()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Nota: processEnemyFire imprime sempre o JSON, independentemente de verbose.
        // Verificamos apenas que a linha "Jogada nº..." não aparece.
        System.setOut(new PrintStream(out));
        move.processEnemyFire(false);
        System.setOut(System.out);

        assertFalse(out.toString().contains("Jogada nº"),
                "Com verbose=false não deve aparecer a linha 'Jogada nº...'.");
    }

    // ─── processEnemyFire — plural/singular das mensagens verbose ────────────

    @Test
    void processEnemyFire_verboseTrue_singleMiss_singularForm() {
        // 1 tiro na água + 2 inválidos => "1 tiro válido: 1 tiro na água"
        Move move = new Move(1, threePositions(), List.of(miss(), invalid(), invalid()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        move.processEnemyFire(true);
        System.setOut(System.out);

        // Forma singular: "1 tiro" (sem 's')
        String output = out.toString();
        assertTrue(output.contains("1 tiro") && !output.contains("1 tiros"),
                "Singular 'tiro' deve ser usado para contagem 1.");
    }

    @Test
    void processEnemyFire_verboseTrue_twoMisses_pluralForm() {
        // 2 tiros na água + 1 inválido
        Move move = new Move(1, threePositions(), List.of(miss(), miss(), invalid()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        move.processEnemyFire(true);
        System.setOut(System.out);

        assertTrue(out.toString().contains("tiros"),
                "Plural 'tiros' deve ser usado para contagem > 1.");
    }

    // ─── processEnemyFire — consistência verbose=true vs verbose=false ───────

    @Test
    void processEnemyFire_verboseTrueAndFalse_returnSameJson() throws Exception {
        IShip barge = new Barge(Compass.NORTH, new Position(2, 2));

        Move move1 = new Move(1, threePositions(), List.of(sink(barge), miss(), miss()));
        Move move2 = new Move(1, threePositions(), List.of(sink(barge), miss(), miss()));

        JsonNode root1 = parseJson(move1.processEnemyFire(true));
        JsonNode root2 = parseJson(move2.processEnemyFire(false));

        assertEquals(root1.get("validShots").asInt(),    root2.get("validShots").asInt());
        assertEquals(root1.get("missedShots").asInt(),   root2.get("missedShots").asInt());
        assertEquals(root1.get("repeatedShots").asInt(), root2.get("repeatedShots").asInt());
        assertEquals(root1.get("outsideShots").asInt(),  root2.get("outsideShots").asInt());
        assertEquals(root1.get("sunkBoats").size(),      root2.get("sunkBoats").size());
    }
}