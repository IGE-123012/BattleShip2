package battleship;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PDFGeneratorTest {

    @Test
    void testGerarRelatorioComJogadas() {

        PDFGenerator generator = new PDFGenerator();

        List<String> jogadas = new ArrayList<>();
        jogadas.add("Linha 1, Coluna 2 - AGUA");
        jogadas.add("Linha 3, Coluna 4 - ACERTO");

        generator.gerarRelatorio(jogadas);

        File pdfFile = new File("data/historico_jogadas.pdf");

        assertTrue(pdfFile.exists(),
                "O ficheiro PDF deveria ser criado");

    }

    @Test
    void testGerarRelatorioListaVazia() {

        PDFGenerator generator = new PDFGenerator();

        List<String> jogadas = new ArrayList<>();

        generator.gerarRelatorio(jogadas);

        File pdfFile = new File("data/historico_jogadas.pdf");

        assertTrue(pdfFile.exists());

    }

    @Test
    void testGerarRelatorioUmaJogada() {

        PDFGenerator generator = new PDFGenerator();

        List<String> jogadas = new ArrayList<>();
        jogadas.add("Linha 5, Coluna 5 - AFUNDADO");

        generator.gerarRelatorio(jogadas);

        File pdfFile = new File("data/historico_jogadas.pdf");

        assertTrue(pdfFile.exists());

    }

    @Test
    void testGerarRelatorioVariasJogadas() {

        PDFGenerator generator = new PDFGenerator();

        List<String> jogadas = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            jogadas.add("Jogada " + i);
        }

        generator.gerarRelatorio(jogadas);

        File pdfFile = new File("data/historico_jogadas.pdf");

        assertTrue(pdfFile.exists());

    }


}