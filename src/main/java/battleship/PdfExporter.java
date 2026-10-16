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

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            float leading = 16.0f;
            contentStream.setLeading(leading);
            contentStream.newLineAtOffset(50, 700);
            float yPosition = 700;

            contentStream.showText("Relatório do Diário de Bordo - Batalha Naval");
            contentStream.newLine();
            contentStream.newLine();
            yPosition -= (2 * leading);

            for (String log : shotLogs) {
                // Paginação: se o texto chegar ao fundo da página, cria uma folha nova
                if (yPosition <= 50) {
                    contentStream.endText();
                    contentStream.close();

                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    contentStream.setLeading(leading);
                    contentStream.newLineAtOffset(50, 700);
                    yPosition = 700;
                }

                contentStream.showText(log);
                contentStream.newLine();
                yPosition -= leading;
            }

            contentStream.endText();
            contentStream.close();

            document.save(filename);
            System.out.println("PDF das rajadas exportado com sucesso para: " + filename);

        } catch (IOException e) {
            System.err.println("Erro na exportação para PDF: " + e.getMessage());
        }
    }
}