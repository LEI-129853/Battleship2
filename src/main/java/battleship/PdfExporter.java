package battleship;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/**
 * Exports the "diário de bordo" (one line per burst) to a PDF file.
 */
public class PdfExporter {

    private static final String TITLE = "Relatório do Diário de Bordo - Batalha Naval";
    private static final float MARGIN = 50f;
    private static final float LINE_HEIGHT = 18f;
    private static final float FONT_SIZE = 12f;

    private PdfExporter() {
    }

    /**
     * Writes the log of bursts to a PDF file, one line per burst, adding pages as needed.
     *
     * @param linhas   the lines of the log (e.g. "Rajada 1 (Manual): 2 Tiros na Água")
     * @param fileName the name of the PDF file to create
     */
    public static void exportShots(List<String> linhas, String fileName) {
        try (PDDocument doc = new PDDocument()) {
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            float top = PDRectangle.A4.getHeight() - MARGIN;

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float y = top;

            y = writeLine(cs, font, TITLE, y);
            y -= LINE_HEIGHT;

            for (String linha : linhas) {
                if (y < MARGIN) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = top;
                }
                y = writeLine(cs, font, linha, y);
            }
            cs.close();

            doc.save(new File(fileName));
            System.out.println("PDF guardado em: " + new File(fileName).getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Erro ao gerar o PDF: " + e.getMessage());
        }
    }

    private static float writeLine(PDPageContentStream cs, PDType1Font font, String text, float y) throws IOException {
        cs.beginText();
        cs.setFont(font, FONT_SIZE);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(sanitize(text));
        cs.endText();
        return y - LINE_HEIGHT;
    }

    /** Keeps only characters the standard Helvetica font can encode (Latin-1). */
    private static String sanitize(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= 32 && c <= 255) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}