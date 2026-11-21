package battleship;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class PdfExporter.
 * Author: LEI-129853
 * Date: 2026-11-21
 * Cyclomatic Complexity:
 * - constructor (private): 1
 * - exportShots(): 4
 * - writeLine() (private): 1
 * - sanitize() (private): 4
 * Note: writeLine() and sanitize() are private; their paths are covered through exportShots().
 */
public class PdfExporterTest {

    @TempDir
    Path tempDir;

    private List<String> linhas;
    private File pdf;

    @BeforeEach
    void setUp() {
        linhas = new ArrayList<>();
        pdf = tempDir.resolve("teste.pdf").toFile();
    }

    @AfterEach
    void tearDown() {
        linhas = null;
        pdf = null;
    }

    // ---------- helpers ----------

    private int countPages(File file) throws IOException {
        try (PDDocument doc = Loader.loadPDF(file)) {
            return doc.getNumberOfPages();
        }
    }

    private String extractText(File file) throws IOException {
        try (PDDocument doc = Loader.loadPDF(file)) {
            return new PDFTextStripper().getText(doc);
        }
    }

    // ---------- constructor (CC = 1) ----------

    @Test
    @DisplayName("constructor: é privado e pode ser invocado por reflexão")
    void constructor() throws Exception {
        Constructor<PdfExporter> c = PdfExporter.class.getDeclaredConstructor();
        c.setAccessible(true);
        assertAll(
                () -> assertTrue(Modifier.isPrivate(c.getModifiers()),
                        "Error: expected private constructor but it is not private"),
                () -> assertNotNull(c.newInstance(),
                        "Error: expected an instance but got null")
        );
    }

    // ---------- exportShots (CC = 4) ----------

    @Test
    @DisplayName("exportShots: lista vazia não entra no ciclo e gera 1 página só com o título")
    void exportShots1() throws IOException {
        PdfExporter.exportShots(linhas, pdf.getPath());

        assertAll(
                () -> assertTrue(pdf.exists(), "Error: expected PDF file to exist but it does not"),
                () -> assertEquals(1, countPages(pdf), "Error: expected 1 page but got " + countPages(pdf)),
                () -> assertTrue(extractText(pdf).contains("Batalha Naval"),
                        "Error: expected title in the PDF but it is missing")
        );
    }

    @Test
    @DisplayName("exportShots: poucas linhas cabem numa página (y >= MARGIN, sem página nova)")
    void exportShots2() throws IOException {
        linhas.add("Rajada 1 (Manual): 2 Tiros na Água");
        linhas.add("Rajada 2 (Manual): 1 Barca ao fundo");
        PdfExporter.exportShots(linhas, pdf.getPath());

        String text = extractText(pdf);
        assertAll(
                () -> assertEquals(1, countPages(pdf), "Error: expected 1 page but got " + countPages(pdf)),
                () -> assertTrue(text.contains("Rajada 1"), "Error: expected 'Rajada 1' in the PDF"),
                () -> assertTrue(text.contains("Rajada 2"), "Error: expected 'Rajada 2' in the PDF")
        );
    }

    @Test
    @DisplayName("exportShots: muitas linhas fazem y < MARGIN e criam uma página nova")
    void exportShots3() throws IOException {
        for (int i = 1; i <= 100; i++) {
            linhas.add("Rajada " + i + " (Manual): 3 Tiros na Água");
        }
        PdfExporter.exportShots(linhas, pdf.getPath());

        String text = extractText(pdf);
        assertAll(
                () -> assertTrue(countPages(pdf) >= 2,
                        "Error: expected at least 2 pages but got " + countPages(pdf)),
                () -> assertTrue(text.contains("Rajada 1 "), "Error: expected first line in the PDF"),
                () -> assertTrue(text.contains("Rajada 100 "), "Error: expected last line in the PDF")
        );
    }

    @Test
    @DisplayName("exportShots: diretoria inexistente provoca IOException, apanhada e reportada na consola")
    void exportShots4() {
        String badPath = tempDir.resolve("nao_existe").resolve("x.pdf").toString();
        linhas.add("linha");

        PrintStream original = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try {
            assertDoesNotThrow(() -> PdfExporter.exportShots(linhas, badPath),
                    "Error: expected the IOException to be caught but it was thrown");
        } finally {
            System.setOut(original);
        }
        assertAll(
                () -> assertTrue(out.toString().contains("Erro ao gerar o PDF"),
                        "Error: expected error message on the console but got: " + out),
                () -> assertFalse(new File(badPath).exists(),
                        "Error: expected no PDF to be created but it exists")
        );
    }

    // ---------- writeLine (CC = 1, private) ----------

    @Test
    @DisplayName("writeLine: cada linha é escrita no PDF")
    void writeLine() throws IOException {
        linhas.add("Linha de teste");
        PdfExporter.exportShots(linhas, pdf.getPath());

        assertTrue(extractText(pdf).contains("Linha de teste"),
                "Error: expected 'Linha de teste' written in the PDF but it is missing");
    }

    // ---------- sanitize (CC = 4, private) ----------

    @Test
    @DisplayName("sanitize: texto só com caracteres Latin-1 válidos fica igual")
    void sanitize1() throws IOException {
        linhas.add("Água ao fundo");
        PdfExporter.exportShots(linhas, pdf.getPath());

        assertTrue(extractText(pdf).contains("Água ao fundo"),
                "Error: expected 'Água ao fundo' unchanged in the PDF");
    }

    @Test
    @DisplayName("sanitize: caracter de controlo (c < 32) é removido")
    void sanitize2() throws IOException {
        linhas.add("A\tB");
        PdfExporter.exportShots(linhas, pdf.getPath());

        assertTrue(extractText(pdf).contains("AB"),
                "Error: expected 'AB' (tab removed) in the PDF");
    }

    @Test
    @DisplayName("sanitize: caracter fora de Latin-1 (c > 255) é removido")
    void sanitize3() throws IOException {
        linhas.add("B€C");
        assertDoesNotThrow(() -> PdfExporter.exportShots(linhas, pdf.getPath()),
                "Error: expected '€' to be removed, but PDFBox threw an exception");

        assertTrue(extractText(pdf).contains("BC"),
                "Error: expected 'BC' (euro sign removed) in the PDF");
    }

    @Test
    @DisplayName("sanitize: linha vazia não entra no ciclo e não causa erro")
    void sanitize4() throws IOException {
        linhas.add("");
        PdfExporter.exportShots(linhas, pdf.getPath());

        assertAll(
                () -> assertTrue(pdf.exists(), "Error: expected PDF file to exist but it does not"),
                () -> assertEquals(1, countPages(pdf), "Error: expected 1 page but got " + countPages(pdf))
        );
    }
}