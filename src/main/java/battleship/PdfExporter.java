package battleship;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.util.List;

public class PdfExporter {

    public static void exportShots(List<String> shotLogs, String filename) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                // Configuração da fonte (Sintaxe compatível com PDFBox 3.x)
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(50, 700);

                contentStream.showText("Relatório do Diário de Bordo - Batalha Naval");
                contentStream.newLine();
                contentStream.newLine();

                for (String log : shotLogs) {
                    contentStream.showText(log);
                    contentStream.newLine();
                }

                contentStream.endText();
            }
            document.save(filename);
            System.out.println("PDF das rajadas exportado com sucesso para: " + filename);

        } catch (IOException e) {
            System.err.println("Erro na exportação para PDF: " + e.getMessage());
        }
    }
}