package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@DisplayName("Testes Unitários para a Barca (Tamanho 1)")
public class BargeTest {

	private Barge barge;
	private IPosition posInicial;

	@BeforeEach
	void setUp() {
		// A direção não afeta a Barca, mas passamos NORTH por defeito
		posInicial = new Position(3, 3);
		barge = new Barge(Compass.NORTH, posInicial);
	}

	@AfterEach
	void tearDown() {
		barge = null;
	}

	@Test
	@DisplayName("Garante que o construtor cria a Barca corretamente com tamanho 1 e na posição certa")
	void testConstructor() {
		List<IPosition> positions = barge.getPositions();

		assertNotNull(barge, "Erro: A instância da Barca não deve ser nula.");
		assertEquals(1, barge.getSize(), "Erro: A Barca deve ter exatamente tamanho 1.");
		assertEquals(1, positions.size(), "Erro: A lista de posições deve ter apenas 1 elemento.");

		// Garante que a posição gravada é exatamente a posição passada no construtor
		assertEquals(new Position(3, 3), positions.get(0), "Erro: A posição da Barca não coincide com a posição dada.");
	}

	@Test
	@DisplayName("Verifica se a Barca flutua quando acabada de criar")
	void testStillFloating1() {
		assertTrue(barge.stillFloating(), "Erro: A Barca deve flutuar inicialmente.");
	}

	@Test
	@DisplayName("Garante que a Barca afunda instantaneamente quando a sua única posição é atingida")
	void testStillFloating2() {
		barge.getPositions().get(0).shoot(); // Dá um tiro na única posição
		assertFalse(barge.stillFloating(), "Erro: A Barca deve afundar após ser atingida.");
	}

	@Test
	@DisplayName("Lança exceção quando construída com uma posição nula")
	void testConstructorWithInvalidInput() {
		assertThrows(NullPointerException.class, () -> new Barge(Compass.NORTH, null),
				"Erro: Devia lançar NullPointerException para uma posição nula.");
	}
}