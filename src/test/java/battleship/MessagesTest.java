package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

public class MessagesTest {

    @Test
    @DisplayName("Teste de idioma Português com chave válida")
    void testEscolherIdiomaPortugues() {
        Scanner scanner = new Scanner("1\n");
        Messages.escolherIdioma(scanner);

        // Usamos "menu.titulo" em vez de "welcome"
        String mensagem = Messages.get("menu.titulo");
        assertNotNull(mensagem, "A chave 'menu.titulo' tem de existir");
        assertEquals("***  Batalha Naval  ***", mensagem);
    }

    @Test
    @DisplayName("Teste de idioma Inglês")
    void testEscolherIdiomaIngles() {
        Scanner scanner = new Scanner("2\n");
        Messages.escolherIdioma(scanner);

        String mensagem = Messages.get("menu.titulo");
        assertNotNull(mensagem);
    }

    @Test
    @DisplayName("Teste de idioma Espanhol")
    void testEscolherIdiomaEspanhol() {
        Scanner scanner = new Scanner("3\n");
        Messages.escolherIdioma(scanner);

        String mensagem = Messages.get("menu.titulo");
        assertNotNull(mensagem);
    }

    @Test
    @DisplayName("Teste de idioma Default")
    void testEscolherIdiomaDefault() {
        Scanner scanner = new Scanner("9\n");
        Messages.escolherIdioma(scanner);

        String mensagem = Messages.get("menu.titulo");
        assertNotNull(mensagem);
    }

    @Test
    @DisplayName("Teste de obtenção de mensagem com parâmetros")
    void testGetComParametros() {
        Scanner scanner = new Scanner("1\n");
        Messages.escolherIdioma(scanner);


        String mensagem = Messages.get("jogo.tempo", 10);

        assertNotNull(mensagem);
        assertTrue(mensagem.contains("10"), "A mensagem deve incluir o valor passado como parâmetro");
    }
}