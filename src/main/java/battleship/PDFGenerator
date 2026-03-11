package battleship;


import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.io.FileNotFoundException;
import java.util.List;

public class PDFGenerator {

    private static final String FILE_PATH = "data/historico_jogadas.pdf";

    /**
     * Recebe a lista de jogadas e gera um ficheiro PDF.
     * @param jogadas Lista de Strings ou objetos que representam os tiros.
     */
    public void gerarRelatorio(List<String> jogadas) {
        try {
            // 1. Configurar o escritor e o documento PDF
            PdfWriter writer = new PdfWriter(FILE_PATH);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 2. Adicionar cabeçalho
            document.add(new Paragraph("Relatório de Jogadas - Battleship2")
                    .setBold()
                    .setFontSize(18));
            document.add(new Paragraph("Histórico detalhado dos tiros realizados:"));

            // 3. Criar uma tabela para as jogadas
            Table table = new Table(1); // 1 coluna
            table.addHeaderCell("Detalhe do Tiro");

            for (String jogada : jogadas) {
                table.addCell(jogada);
            }

            document.add(table);

            // 4. Fechar o documento
            document.close();
            System.out.println("PDF gerado com sucesso em: " + FILE_PATH);

        } catch (FileNotFoundException e) {
            System.err.println("Erro ao criar o ficheiro PDF: " + e.getMessage());
        }
    }
}
