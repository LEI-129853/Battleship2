package battleship;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários da classe Move.
 * Complexidade ciclomática:
 * - Move (construtor), toString, getNumber, getShots, getShotResults, getSummary: 1
 * - processEnemyFire: muito elevada (> 30) — principal candidato a refabricação (Long Method)
 */
class MoveTest {

    // ---------- auxiliares para construir resultados de tiros ----------
    private static final IShip GALEAO = new Galleon(Compass.NORTH, new Position(0, 0));
    private static final IShip BARCA = new Barge(Compass.NORTH, new Position(9, 9));

    private static IGame.ShotResult invalid()        { return new IGame.ShotResult(false, false, null, false); }
    private static IGame.ShotResult repeated()       { return new IGame.ShotResult(true, true, null, false); }
    private static IGame.ShotResult miss()           { return new IGame.ShotResult(true, false, null, false); }
    private static IGame.ShotResult hit(IShip s)     { return new IGame.ShotResult(true, false, s, false); }
    private static IGame.ShotResult sunk(IShip s)    { return new IGame.ShotResult(true, false, s, true); }

    private static Move move(IGame.ShotResult... results) {
        List<IPosition> shots = new ArrayList<>();
        for (int i = 0; i < results.length; i++) {
            shots.add(new Position(i, i));
        }
        return new Move(1, shots, Arrays.asList(results));
    }

    private static JsonNode json(String s) throws Exception {
        return new ObjectMapper().readTree(s);
    }

    // ---------- métodos simples ----------
    @Nested
    @DisplayName("Getters e toString")
    class Getters {

        @Test
        @DisplayName("Getters devolvem os valores passados ao construtor")
        void getters() {
            List<IPosition> shots = List.of(new Position(1, 1), new Position(2, 2), new Position(3, 3));
            List<IGame.ShotResult> results = List.of(miss(), miss(), miss());
            Move m = new Move(7, shots, results);

            assertEquals(7, m.getNumber());
            assertSame(shots, m.getShots());
            assertSame(results, m.getShotResults());
            assertEquals("", m.getSummary());
        }

        @Test
        @DisplayName("toString mostra número, nº de tiros e nº de resultados")
        void testToString() {
            Move m = move(miss(), miss());
            assertEquals("Move{number=1, shots=2, results=2}", m.toString());
        }
    }

    // ---------- resumo para o diário de bordo (verbose = false) ----------
    @Nested
    @DisplayName("processEnemyFire(false) — resumo do diário de bordo")
    class Summary {

        @Test
        @DisplayName("3 tiros na água (plural)")
        void threeMisses() {
            Move m = move(miss(), miss(), miss());
            m.processEnemyFire(false);
            assertEquals("3 Tiros na Água", m.getSummary());
        }

        @Test
        @DisplayName("1 tiro na água (singular) + 2 tiros exteriores (plural)")
        void oneMissTwoOutside() {
            Move m = move(miss(), invalid(), invalid());
            m.processEnemyFire(false);
            assertEquals("1 Tiro na Água, 2 Tiros Exteriores", m.getSummary());
        }

        @Test
        @DisplayName("1 tiro exterior (singular)")
        void oneOutside() {
            Move m = move(miss(), miss(), invalid());
            m.processEnemyFire(false);
            assertEquals("2 Tiros na Água, 1 Tiro Exterior", m.getSummary());
        }

        @Test
        @DisplayName("Acertos num navio não afundado (singular e plural)")
        void hitsOnFloatingShip() {
            Move one = move(hit(GALEAO), miss(), miss());
            one.processEnemyFire(false);
            assertTrue(one.getSummary().contains("1 Tiro na Galeao"));

            Move two = move(hit(GALEAO), hit(GALEAO), miss());
            two.processEnemyFire(false);
            assertTrue(two.getSummary().contains("2 Tiros na Galeao"));
        }

        @Test
        @DisplayName("Navio afundado aparece como 'ao fundo' e não como acerto")
        void sunkShip() {
            Move m = move(sunk(BARCA), miss(), miss());
            m.processEnemyFire(false);
            String s = m.getSummary();
            assertTrue(s.contains("1 Barca ao fundo"));
            assertFalse(s.contains("Tiro na Barca"));
        }

        @Test
        @DisplayName("Tiros repetidos (singular e plural)")
        void repeatedShots() {
            Move one = move(repeated(), miss(), miss());
            one.processEnemyFire(false);
            assertTrue(one.getSummary().endsWith("1 Tiro Repetido"));

            Move three = move(repeated(), repeated(), repeated());
            three.processEnemyFire(false);
            assertEquals("3 Tiros Repetidos", three.getSummary());
        }
    }

    // ---------- mensagem detalhada (verbose = true) ----------
    @Nested
    @DisplayName("processEnemyFire(true) — mensagem detalhada")
    class Verbose {

        @Test
        @DisplayName("Só tiros repetidos (validShots == 0 && repeatedShots > 0)")
        void onlyRepeated() {
            Move m = move(repeated(), repeated(), repeated());
            m.processEnemyFire(true);
            assertEquals("3 tiros repetidos", m.getSummary());
        }

        @Test
        @DisplayName("1 repetido (singular) + 2 exteriores, sem tiros válidos")
        void oneRepeatedTwoOutside() {
            Move m = move(repeated(), invalid(), invalid());
            m.processEnemyFire(true);
            assertEquals("1 tiro repetido, 2 tiros exteriores", m.getSummary());
        }

        @Test
        @DisplayName("Todos inválidos: só tiros exteriores (output vazio antes)")
        void allInvalid() {
            Move m = move(invalid(), invalid(), invalid());
            m.processEnemyFire(true);
            assertEquals("3 tiros exteriores", m.getSummary());
        }

        @Test
        @DisplayName("1 tiro válido na água + 2 exteriores (singulares)")
        void oneValidMiss() {
            Move m = move(miss(), invalid(), invalid());
            m.processEnemyFire(true);
            assertEquals("1 tiro válido: 1 tiro na água, 2 tiros exteriores", m.getSummary());
        }

        @Test
        @DisplayName("3 tiros válidos na água (plurais)")
        void threeValidMisses() {
            Move m = move(miss(), miss(), miss());
            m.processEnemyFire(true);
            assertEquals("3 tiros válidos: 3 tiros na água", m.getSummary());
        }

        @Test
        @DisplayName("1 navio ao fundo (singular) + tiros na água")
        void oneSunkWithMisses() {
            Move m = move(sunk(BARCA), miss(), miss());
            m.processEnemyFire(true);
            assertEquals("3 tiros válidos: 1 Barca ao fundo + 2 tiros na água", m.getSummary());
        }

        @Test
        @DisplayName("2 navios iguais ao fundo (plural) + acerto, sem água: remove o '+' final")
        void twoSunkAndHitNoMiss() {
            Move m = move(sunk(BARCA), sunk(BARCA), hit(GALEAO));
            m.processEnemyFire(true);
            String s = m.getSummary();
            assertTrue(s.startsWith("3 tiros válidos: "));
            assertTrue(s.contains("2 Barcas ao fundo"));
            assertTrue(s.contains("1 tiro num(a) Galeao"));
            assertFalse(s.trim().endsWith("+"));
        }

        @Test
        @DisplayName("Acertos (plural) sem água + repetido: ', 1 tiro repetido'")
        void hitsAndRepeated() {
            Move m = move(hit(GALEAO), hit(GALEAO), repeated());
            m.processEnemyFire(true);
            String s = m.getSummary();
            assertTrue(s.startsWith("2 tiros válidos: 2 tiros num(a) Galeao"));
            assertTrue(s.endsWith(", 1 tiro repetido"));
        }

        @Test
        @DisplayName("Acerto + água + 2 repetidos (plural)")
        void hitMissRepeated() {
            Move m = move(hit(GALEAO), miss(), repeated(), repeated());
            m.processEnemyFire(true);
            assertEquals("2 tiros válidos: 1 tiro num(a) Galeao + 1 tiro na água, 2 tiros repetidos",
                    m.getSummary());
        }
    }

    // ---------- JSON devolvido ----------
    @Nested
    @DisplayName("JSON devolvido por processEnemyFire")
    class Json {

        @Test
        @DisplayName("Contagens de tiros no JSON")
        void counts() throws Exception {
            JsonNode n = json(move(miss(), repeated(), invalid()).processEnemyFire(false));
            assertEquals(1, n.get("validShots").asInt());
            assertEquals(1, n.get("repeatedShots").asInt());
            assertEquals(1, n.get("missedShots").asInt());
            assertEquals(1, n.get("outsideShots").asInt());
            assertEquals(0, n.get("sunkBoats").size());
            assertEquals(0, n.get("hitsOnBoats").size());
        }

        @Test
        @DisplayName("sunkBoats e hitsOnBoats (navio afundado não aparece em hitsOnBoats)")
        void boats() throws Exception {
            JsonNode n = json(move(sunk(BARCA), hit(GALEAO), hit(GALEAO)).processEnemyFire(false));

            assertEquals(1, n.get("sunkBoats").size());
            assertEquals("Barca", n.get("sunkBoats").get(0).get("type").asText());
            assertEquals(1, n.get("sunkBoats").get(0).get("count").asInt());

            assertEquals(1, n.get("hitsOnBoats").size());
            assertEquals("Galeao", n.get("hitsOnBoats").get(0).get("type").asText());
            assertEquals(2, n.get("hitsOnBoats").get(0).get("hits").asInt());
        }
    }
}