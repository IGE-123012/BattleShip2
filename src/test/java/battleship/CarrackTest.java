package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@DisplayName("Testes Unitários para a Nau (Tamanho 3)")
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

    @Test
    @DisplayName("Garante que a Nau a Norte é criada com 3 posições corretas")
    void testConstructorNorth() {
        List<IPosition> positions = carrack.getPositions();
        assertNotNull(carrack, "Erro: A instância de Carrack não deve ser nula.");
        assertEquals(3, positions.size(), "Erro: A Nau a Norte deve ter 3 posições.");
        assertEquals(new Position(5, 5), positions.get(0));
        assertEquals(new Position(6, 5), positions.get(1));
        assertEquals(new Position(7, 5), positions.get(2));
    }

    @Test
    @DisplayName("Garante que a Nau a Sul é criada com 3 posições corretas")
    void testConstructorSouth() {
        Carrack carrackSouth = new Carrack(Compass.SOUTH, new Position(5, 5));
        List<IPosition> positions = carrackSouth.getPositions();
        assertEquals(3, positions.size(), "Erro: A Nau a Sul deve ter 3 posições.");
        assertEquals(new Position(5, 5), positions.get(0));
        assertEquals(new Position(6, 5), positions.get(1));
        assertEquals(new Position(7, 5), positions.get(2));
    }

    @Test
    @DisplayName("Garante que a Nau a Este é criada com 3 posições corretas")
    void testConstructorEast() {
        Carrack carrackEast = new Carrack(Compass.EAST, new Position(5, 5));
        List<IPosition> positions = carrackEast.getPositions();
        assertEquals(3, positions.size(), "Erro: A Nau a Este deve ter 3 posições.");
        assertEquals(new Position(5, 5), positions.get(0));
        assertEquals(new Position(5, 6), positions.get(1));
        assertEquals(new Position(5, 7), positions.get(2));
    }

    @Test
    @DisplayName("Garante que a Nau a Oeste é criada com 3 posições corretas")
    void testConstructorWest() {
        Carrack carrackWest = new Carrack(Compass.WEST, new Position(5, 5));
        List<IPosition> positions = carrackWest.getPositions();
        assertEquals(3, positions.size(), "Erro: A Nau a Oeste deve ter 3 posições.");
        assertEquals(new Position(5, 5), positions.get(0));
        assertEquals(new Position(5, 6), positions.get(1));
        assertEquals(new Position(5, 7), positions.get(2));
    }

    @Test
    @DisplayName("Verifica se a Nau flutua quando acabada de criar")
    void testStillFloating1() {
        assertTrue(carrack.stillFloating(), "Erro: A Nau deve flutuar inicialmente.");
    }

    @Test
    @DisplayName("Garante que a Nau afunda quando todas as posições são atingidas")
    void testStillFloating2() {
        carrack.getPositions().forEach(IPosition::shoot);
        assertFalse(carrack.stillFloating(), "Erro: A Nau deve afundar após os 3 tiros.");
    }

    @Test
    @DisplayName("Lança exceção quando construída com dados nulos")
    void testConstructorWithInvalidInput() {
        assertThrows(NullPointerException.class, () -> new Carrack(null, null),
                "Erro: Devia lançar NullPointerException para input nulo.");
    }
}