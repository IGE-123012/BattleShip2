package battleship;

import org.junit.jupiter.api.Test;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class MessagesTest {

    @Test
    void testEscolherIdiomaPortugues() {

        Scanner scanner = new Scanner("1");

        Messages.escolherIdioma(scanner);

        String mensagem = Messages.get("welcome");

        assertNotNull(mensagem);

    }

    @Test
    void testEscolherIdiomaIngles() {

        Scanner scanner = new Scanner("2");

        Messages.escolherIdioma(scanner);

        String mensagem = Messages.get("welcome");

        assertNotNull(mensagem);

    }

    @Test
    void testEscolherIdiomaEspanhol() {

        Scanner scanner = new Scanner("3");

        Messages.escolherIdioma(scanner);

        String mensagem = Messages.get("welcome");

        assertNotNull(mensagem);

    }


    @Test
    void testEscolherIdiomaDefault() {

        Scanner scanner = new Scanner("9");

        Messages.escolherIdioma(scanner);

        String mensagem = Messages.get("welcome");

        assertNotNull(mensagem);

    }


    @Test
    void testGetComParametros() {

        Scanner scanner = new Scanner("1");

        Messages.escolherIdioma(scanner);

        String mensagem = Messages.get("score", 10);

        assertNotNull(mensagem);

    }





}