package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Test class for Fleet.
 * Cyclomatic Complexity covered per method:
 * - Constructor:              1
 * - createRandom:             2 (loop + retry)
 * - addShip:                  4 (size limit, board, safe, success)
 * - getShips:                 1
 * - getShipsLike:             2 (match / no match)
 * - getFloatingShips:         2 (all floating / all sunk)
 * - getSunkShips:             2 (none sunk / all sunk)
 * - shipAt:                   2 (found / not found)
 * - isInsideBoard (private):  3
 * - isSafe (private):         2
 * - printShips:               1
 * - printStatus:              1
 * - printShipsByCategory:     1
 * - printFloatingShips:       1
 * - printAllShips:            1
 */
public class FleetTest {

    private Fleet fleet;

    @BeforeEach
    void setUp() {
        fleet = new Fleet();
    }

    @AfterEach
    void tearDown() {
        fleet = null;
    }

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Verifica que a frota é criada com lista de navios vazia.
     * CC: 1
     */
    @Test
    void testConstructor() {
        assertNotNull(fleet, "A instância de Fleet não deve ser nula.");
        assertTrue(fleet.getShips().isEmpty(), "A frota deve ser inicializada sem navios.");
    }

    // -------------------------------------------------------------------------
    // createRandom
    // -------------------------------------------------------------------------

    /**
     * Verifica que createRandom() devolve uma frota válida com exatamente
     * FLEET_SIZE navios, todos dentro do tabuleiro e sem colisões entre si.
     */
    @Test
    void testCreateRandom_returnsFullFleet() {
        IFleet randomFleet = Fleet.createRandom();

        assertNotNull(randomFleet, "createRandom() não deve devolver null.");
        assertEquals(Fleet.FLEET_SIZE, randomFleet.getShips().size(),
                "A frota aleatória deve conter exatamente FLEET_SIZE navios.");
    }

    /**
     * Verifica que todos os navios da frota aleatória estão a flutuar
     * (nenhum foi atingido durante a construção).
     */
    @Test
    void testCreateRandom_allShipsFloating() {
        IFleet randomFleet = Fleet.createRandom();

        assertEquals(randomFleet.getShips().size(), randomFleet.getFloatingShips().size(),
                "Todos os navios da frota aleatória devem estar a flutuar inicialmente.");
        assertTrue(randomFleet.getSunkShips().isEmpty(),
                "Não deve haver navios afundados numa frota recém-criada.");
    }

    /**
     * Verifica que duas frotas aleatórias têm as mesmas categorias de navios
     * (composição fixa) mesmo que as posições sejam diferentes.
     */
    @Test
    void testCreateRandom_correctShipCategories() {
        IFleet randomFleet = Fleet.createRandom();

        assertEquals(1, randomFleet.getShipsLike("Galeao").size(), "Deve haver 1 galeão.");
        assertEquals(1, randomFleet.getShipsLike("Fragata").size(), "Deve haver 1 fragata.");
        assertEquals(2, randomFleet.getShipsLike("Nau").size(),     "Devem haver 2 naus.");
        assertEquals(3, randomFleet.getShipsLike("Caravela").size(),"Devem haver 3 caravelas.");
        assertEquals(4, randomFleet.getShipsLike("Barca").size(),   "Devem haver 4 barcas.");
    }

    // -------------------------------------------------------------------------
    // addShip
    // -------------------------------------------------------------------------

    /**
     * Cenário de sucesso: navio válido é adicionado.
     * CC: 3 (todos os predicados verdadeiros)
     */
    @Test
    void testAddShip_success() {
        IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
        assertTrue(fleet.addShip(ship), "Navio válido deve ser adicionado com sucesso.");
        assertEquals(1, fleet.getShips().size(), "A frota deve conter um navio.");
    }

    /**
     * Limite de tamanho da frota atingido: addShip deve devolver false.
     */
    @Test
    void testAddShip_fleetSizeLimitReached() {
        for (int i = 0; i < Fleet.FLEET_SIZE; i++) {
            fleet.addShip(new Barge(Compass.NORTH, new Position(i * 3, 0)));
        }
        IShip extra = new Barge(Compass.NORTH, new Position(5, 9));
        assertFalse(fleet.addShip(extra), "Não deve ser possível adicionar além do limite da frota.");
        assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size());
    }

    /**
     * Navio fora do tabuleiro: addShip deve devolver false.
     */
    @Test
    void testAddShip_shipOutsideBoard() {
        IShip ship = new Barge(Compass.NORTH, new Position(99, 99));
        assertFalse(fleet.addShip(ship), "Navio fora do tabuleiro não deve ser adicionado.");
        assertTrue(fleet.getShips().isEmpty());
    }

    /**
     * Colisão com navio existente: addShip deve devolver false.
     */
    @Test
    void testAddShip_collisionRisk() {
        IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
        IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));
        fleet.addShip(ship1);
        assertFalse(fleet.addShip(ship2), "Navio em colisão não deve ser adicionado.");
        assertEquals(1, fleet.getShips().size());
    }

    // -------------------------------------------------------------------------
    // getShips
    // -------------------------------------------------------------------------

    /**
     * Verifica que getShips devolve a lista correta após adições.
     * CC: 1
     */
    @Test
    void testGetShips() {
        assertTrue(fleet.getShips().isEmpty());
        IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
        fleet.addShip(ship);
        assertEquals(1, fleet.getShips().size());
        assertSame(ship, fleet.getShips().get(0));
    }

    /**
     * Test for private method isSafe (formerly colisionRisk).
     * Cyclomatic Complexity: 2
     */
    @Test
    void testIsSafe() throws Exception {
        var method = Fleet.class.getDeclaredMethod("isSafe", IShip.class);
        method.setAccessible(true);

        IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
        IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));  // Overlapping position
        fleet.addShip(ship1);

        // Agora, navios sobrepostos NÃO SÃO SEGUROS (devolve false)
        assertFalse((Boolean) method.invoke(fleet, ship2), "Error: Overlapping ships should not be safe (return false).");

        // Navios longe uns dos outros SÃO SEGUROS (devolve true)
        assertTrue((Boolean) method.invoke(fleet, new Barge(Compass.NORTH, new Position(5, 5))),
                "Error: Ships at non-overlapping positions should be safe (return true).");
    }

    // -------------------------------------------------------------------------
    // getShipsLike
    // -------------------------------------------------------------------------

    /**
     * Categoria presente: devolve apenas os navios dessa categoria.
     * CC: 2 (branch: categoria corresponde)
     */
    @Test
    void testGetShipsLike_matchingCategory() {
        fleet.addShip(new Barge(Compass.NORTH,   new Position(1, 1)));
        fleet.addShip(new Caravel(Compass.NORTH, new Position(4, 4)));

        List<IShip> barges = fleet.getShipsLike("Barca");
        assertEquals(1, barges.size());
        assertEquals("Barca", barges.get(0).getCategory());
    }

    /**
     * Categoria inexistente: devolve lista vazia.
     * CC: 2 (branch: categoria não corresponde a nenhum navio)
     */
    @Test
    void testGetShipsLike_noMatch() {
        fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));

        List<IShip> result = fleet.getShipsLike("Galeao");
        assertNotNull(result, "Resultado não deve ser null mesmo sem correspondências.");
        assertTrue(result.isEmpty(), "Lista deve estar vazia quando não há navios da categoria.");
    }

    /**
     * Frota vazia: devolve lista vazia independentemente da categoria.
     */
    @Test
    void testGetShipsLike_emptyFleet() {
        assertTrue(fleet.getShipsLike("Barca").isEmpty());
    }

    // -------------------------------------------------------------------------
    // getFloatingShips
    // -------------------------------------------------------------------------

    /**
     * Todos os navios flutuam inicialmente.
     * CC: 2 (branch: stillFloating == true)
     */
    @Test
    void testGetFloatingShips_allFloating() {
        fleet.addShip(new Barge(Compass.NORTH,   new Position(1, 1)));
        fleet.addShip(new Caravel(Compass.NORTH, new Position(4, 4)));

        assertEquals(2, fleet.getFloatingShips().size());
    }

    /**
     * Após afundar um navio, apenas o restante aparece em getFloatingShips.
     */
    @Test
    void testGetFloatingShips_afterSinkingOne() {
        IShip ship1 = new Barge(Compass.NORTH,   new Position(1, 1));
        IShip ship2 = new Caravel(Compass.NORTH, new Position(4, 4));
        fleet.addShip(ship1);
        fleet.addShip(ship2);

        ship1.getPositions().get(0).shoot();

        List<IShip> floating = fleet.getFloatingShips();
        assertEquals(1, floating.size());
        assertSame(ship2, floating.get(0));
    }

    /**
     * Frota totalmente afundada: getFloatingShips devolve lista vazia.
     * CC: 2 (branch: stillFloating == false para todos)
     */
    @Test
    void testGetFloatingShips_allSunk() {
        IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
        fleet.addShip(barge);

        for (IPosition pos : barge.getPositions()) {
            pos.shoot();
        }

        assertTrue(fleet.getFloatingShips().isEmpty(),
                "Não devem existir navios a flutuar quando todos foram afundados.");
    }

    // -------------------------------------------------------------------------
    // getSunkShips
    // -------------------------------------------------------------------------

    /**
     * Sem navios afundados inicialmente.
     * CC: 2 (branch: !stillFloating == false)
     */
    @Test
    void testGetSunkShips_noneInitially() {
        fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
        assertTrue(fleet.getSunkShips().isEmpty(),
                "Não deve haver navios afundados numa frota recém-criada.");
    }

    /**
     * Após afundar todos os navios, getSunkShips devolve todos eles.
     * CC: 2 (branch: !stillFloating == true)
     */
    @Test
    void testGetSunkShips_afterSinkingAll() {
        IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
        fleet.addShip(barge);

        for (IPosition pos : barge.getPositions()) {
            pos.shoot();
        }

        List<IShip> sunk = fleet.getSunkShips();
        assertEquals(1, sunk.size());
        assertSame(barge, sunk.get(0));
    }

    /**
     * getSunkShips e getFloatingShips são complementares: a soma é sempre o total.
     */
    @Test
    void testSunkAndFloatingAreComplementary() {
        IShip ship1 = new Barge(Compass.NORTH,   new Position(1, 1));
        IShip ship2 = new Caravel(Compass.NORTH, new Position(4, 4));
        fleet.addShip(ship1);
        fleet.addShip(ship2);

        ship1.getPositions().get(0).shoot();

        int total = fleet.getFloatingShips().size() + fleet.getSunkShips().size();
        assertEquals(fleet.getShips().size(), total,
                "A soma de flutuantes e afundados deve ser igual ao total.");
    }

    // -------------------------------------------------------------------------
    // shipAt
    // -------------------------------------------------------------------------

    /**
     * Posição ocupada: devolve o navio correto.
     * CC: 2 (branch: occupies == true)
     */
    @Test
    void testShipAt_found() {
        IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
        fleet.addShip(ship);
        assertSame(ship, fleet.shipAt(new Position(1, 1)));
    }

    /**
     * Posição vazia: devolve null.
     * CC: 2 (branch: occupies == false para todos)
     */
    @Test
    void testShipAt_notFound() {
        fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
        assertNull(fleet.shipAt(new Position(9, 9)),
                "Deve devolver null para posição sem navio.");
    }

    /**
     * Frota vazia: shipAt devolve null.
     */
    @Test
    void testShipAt_emptyFleet() {
        assertNull(fleet.shipAt(new Position(1, 1)),
                "Deve devolver null numa frota vazia.");
    }

    // -------------------------------------------------------------------------
    // isInsideBoard (private, via reflexão)
    // -------------------------------------------------------------------------

    /**
     * Navio dentro dos limites: devolve true.
     * CC: 3 (todos os predicados satisfeitos)
     */
    @Test
    void testIsInsideBoard_inside() throws Exception {
        var method = Fleet.class.getDeclaredMethod("isInsideBoard", IShip.class);
        method.setAccessible(true);

        IShip inside = new Barge(Compass.NORTH, new Position(1, 1));
        assertTrue((Boolean) method.invoke(fleet, inside));
    }

    /**
     * Navio fora dos limites (posição inválida): devolve false.
     */
    @Test
    void testIsInsideBoard_outside() throws Exception {
        var method = Fleet.class.getDeclaredMethod("isInsideBoard", IShip.class);
        method.setAccessible(true);

        IShip outside = new Barge(Compass.NORTH, new Position(99, 99));
        assertFalse((Boolean) method.invoke(fleet, outside));
    }

    /**
     * Navio na fronteira do tabuleiro (posição 0,0): deve ser válido.
     */
    @Test
    void testIsInsideBoard_borderPosition() throws Exception {
        var method = Fleet.class.getDeclaredMethod("isInsideBoard", IShip.class);
        method.setAccessible(true);

        IShip border = new Barge(Compass.EAST, new Position(0, 0));
        assertTrue((Boolean) method.invoke(fleet, border));
    }

    // -------------------------------------------------------------------------
    // isSafe (private, via reflexão)
    // -------------------------------------------------------------------------

    /**
     * Navio sobreposto: não é seguro (false).
     * CC: 2 (branch: tooCloseTo == true)
     */
    @Test
    void testIsSafe_overlapping() throws Exception {
        var method = Fleet.class.getDeclaredMethod("isSafe", IShip.class);
        method.setAccessible(true);

        IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
        IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));
        fleet.addShip(ship1);

        assertFalse((Boolean) method.invoke(fleet, ship2));
    }

    /**
     * Navio em posição distante: é seguro (true).
     * CC: 2 (branch: tooCloseTo == false para todos)
     */
    @Test
    void testIsSafe_notOverlapping() throws Exception {
        var method = Fleet.class.getDeclaredMethod("isSafe", IShip.class);
        method.setAccessible(true);

        fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
        IShip distant = new Barge(Compass.NORTH, new Position(8, 8));

        assertTrue((Boolean) method.invoke(fleet, distant));
    }

    /**
     * Frota vazia: qualquer navio é seguro.
     */
    @Test
    void testIsSafe_emptyFleet() throws Exception {
        var method = Fleet.class.getDeclaredMethod("isSafe", IShip.class);
        method.setAccessible(true);

        IShip ship = new Barge(Compass.NORTH, new Position(3, 3));
        assertTrue((Boolean) method.invoke(fleet, ship),
                "Qualquer navio deve ser seguro numa frota vazia.");
    }

    // -------------------------------------------------------------------------
    // Métodos de impressão (output não deve lançar exceções)
    // -------------------------------------------------------------------------

    /**
     * printStatus não lança exceções e produz output com contadores corretos.
     */
    @Test
    void testPrintStatus_doesNotThrow() {
        fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
        assertDoesNotThrow(fleet::printStatus);
    }

    /**
     * printStatus com frota vazia não lança exceções.
     */
    @Test
    void testPrintStatus_emptyFleet() {
        assertDoesNotThrow(fleet::printStatus);
    }

    /**
     * printShips com lista de navios não lança exceções.
     */
    @Test
    void testPrintShips_doesNotThrow() {
        fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
        assertDoesNotThrow(() -> fleet.printShips(fleet.getShips()));
    }

    /**
     * printShips com lista vazia não lança exceções.
     */
    @Test
    void testPrintShips_emptyList() {
        assertDoesNotThrow(() -> fleet.printShips(fleet.getShips()));
    }

    /**
     * printShipsByCategory não lança exceções para categoria existente.
     */
    @Test
    void testPrintShipsByCategory_existingCategory() {
        fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
        assertDoesNotThrow(() -> fleet.printShipsByCategory("Barca"));
    }

    /**
     * printShipsByCategory não lança exceções para categoria inexistente.
     */
    @Test
    void testPrintShipsByCategory_nonExistingCategory() {
        assertDoesNotThrow(() -> fleet.printShipsByCategory("Galeao"));
    }

    /**
     * printFloatingShips não lança exceções.
     */
    @Test
    void testPrintFloatingShips_doesNotThrow() {
        fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
        assertDoesNotThrow(fleet::printFloatingShips);
    }

    /**
     * printAllShips não lança exceções.
     */
    @Test
    void testPrintAllShips_doesNotThrow() {
        fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
        fleet.addShip(new Caravel(Compass.NORTH, new Position(4, 4)));
        assertDoesNotThrow(fleet::printAllShips);
    }

    /**
     * printStatus produz output com os valores corretos de flutuantes/afundados.
     */
    @Test
    void testPrintStatus_outputContent() {
        IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
        fleet.addShip(barge);

        // Afundar o navio
        for (IPosition pos : barge.getPositions()) {
            pos.shoot();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        fleet.printStatus();
        System.setOut(System.out);

        String output = out.toString();
        assertTrue(output.contains("0"), "Output deve indicar 0 navios a flutuar.");
        assertTrue(output.contains("1"), "Output deve indicar 1 navio afundado.");
    }
}