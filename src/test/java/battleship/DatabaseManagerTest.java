package battleship;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

public class DatabaseManagerTest {

    @Test
    void testInicializarBaseDeDados() {

        DatabaseManager.inicializarBaseDeDados();

        // Verificar se o ficheiro foi criado
        File dbFile = new File("historico_batalha.db");

        assertTrue(dbFile.exists(),
                "A base de dados deveria ser criada");

    }

    @Test
    void testRegistarJogada() {

        // Garantir que a base existe
        DatabaseManager.inicializarBaseDeDados();

        // Executar método
        DatabaseManager.registarJogada(3, 5, "ACERTO");

        // Se não lançar exceção, o teste passa
        assertTrue(true);

    }

    @Test
    void testRegistarJogadaValoresDiferentes() {

        DatabaseManager.inicializarBaseDeDados();

        DatabaseManager.registarJogada(0, 0, "AGUA");

        assertTrue(true);

    }

    @Test
    void testRegistarJogadaResultadoVazio() {

        DatabaseManager.inicializarBaseDeDados();

        DatabaseManager.registarJogada(2, 2, "");

        assertTrue(true);

    }
}