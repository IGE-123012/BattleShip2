package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@DisplayName("Testes Unitários para a Caravela (Tamanho 2)")
public class CaravelTest {

	private Caravel caravel;

	@BeforeEach
	void setUp() {
		caravel = new Caravel(Compass.NORTH, new Position(5, 5));
	}

	@AfterEach
	void tearDown() {
		caravel = null;
	}

	@Test
	@DisplayName("Garante que a Caravela a Norte é criada com 2 posições corretas")
	void testConstructorNorth() {
		List<IPosition> positions = caravel.getPositions();
		assertNotNull(caravel, "Erro: A instância de Caravel não deve ser nula.");
		assertEquals(2, positions.size(), "Erro: A Caravela a Norte deve ter 2 posições.");
		assertEquals(new Position(5, 5), positions.get(0));
		assertEquals(new Position(6, 5), positions.get(1));
	}

	@Test
	@DisplayName("Garante que a Caravela a Sul é criada com 2 posições corretas")
	void testConstructorSouth() {
		Caravel caravelSouth = new Caravel(Compass.SOUTH, new Position(5, 5));
		List<IPosition> positions = caravelSouth.getPositions();
		assertEquals(2, positions.size(), "Erro: A Caravela a Sul deve ter 2 posições.");
		assertEquals(new Position(5, 5), positions.get(0));
		assertEquals(new Position(6, 5), positions.get(1));
	}

	@Test
	@DisplayName("Garante que a Caravela a Este é criada com 2 posições corretas")
	void testConstructorEast() {
		Caravel caravelEast = new Caravel(Compass.EAST, new Position(5, 5));
		List<IPosition> positions = caravelEast.getPositions();
		assertEquals(2, positions.size(), "Erro: A Caravela a Este deve ter 2 posições.");
		assertEquals(new Position(5, 5), positions.get(0));
		assertEquals(new Position(5, 6), positions.get(1));
	}

	@Test
	@DisplayName("Garante que a Caravela a Oeste é criada com 2 posições corretas")
	void testConstructorWest() {
		Caravel caravelWest = new Caravel(Compass.WEST, new Position(5, 5));
		List<IPosition> positions = caravelWest.getPositions();
		assertEquals(2, positions.size(), "Erro: A Caravela a Oeste deve ter 2 posições.");
		assertEquals(new Position(5, 5), positions.get(0));
		assertEquals(new Position(5, 6), positions.get(1));
	}

	@Test
	@DisplayName("Verifica se a Caravela flutua quando acabada de criar")
	void testStillFloating1() {
		assertTrue(caravel.stillFloating(), "Erro: A Caravela deve flutuar inicialmente.");
	}

	@Test
	@DisplayName("Garante que a Caravela afunda quando todas as posições são atingidas")
	void testStillFloating2() {
		caravel.getPositions().forEach(IPosition::shoot);
		assertFalse(caravel.stillFloating(), "Erro: A Caravela deve afundar após os 2 tiros.");
	}

	@Test
	@DisplayName("Lança exceção quando construída com dados nulos")
	void testConstructorWithInvalidInput() {
		assertThrows(NullPointerException.class, () -> new Caravel(null, null),
				"Erro: Devia lançar NullPointerException para input nulo.");
	}
}