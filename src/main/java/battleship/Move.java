package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Shot
 *
 * @author Your Name
 * Date: 20/02/2026
 * Time: 19:39
 */
public class Move implements IMove {

	//-------------------------------------------------------------------
	private final int number;
	private final List<IPosition> shots;
	private final List<IGame.ShotResult> shotResults;
	private String summary = ""; // NOVO
	private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	//-------------------------------------------------------------------
	public Move(int moveNumber, List<IPosition> moveShots, List<IGame.ShotResult> moveResults) {
		this.number = moveNumber;
		this.shots = moveShots;
		this.shotResults = moveResults;
	}

	@Override
	public String toString() {
		return "Move{" +
				"number=" + number +
				", shots=" + shots.size() +
				", results=" + shotResults.size() +
				'}';
	}

	@Override
	public int getNumber() {
		return this.number;
	}

	@Override
	public List<IPosition> getShots() {
		return this.shots;
	}

	@Override
	public List<IGame.ShotResult> getShotResults() {
		return this.shotResults;
	}

	// NOVO
	@Override
	public String getSummary() {
		return this.summary;
	}

	/**
	 * Processes the results of enemy fire on the game board, analyzing the outcomes of shots,
	 * such as valid shots, repeated shots, missed shots, hits on ships, and sunk ships. It can
	 * also display a detailed summary of the shot results if verbose mode is activated.
	 * The counting and the messages are done by {@link ShotReport}.
	 *
	 * @param verbose a boolean indicating whether a detailed summary should be printed to the console
	 *                for the processed enemy fire data.
	 * @return a JSON-formatted string that encapsulates the results, including counts of valid shots,
	 *         repeated shots, missed shots, shots outside the game board, and details of hits and
	 *         sunk ships.
	 */
	@Override
	public String processEnemyFire(boolean verbose) {
		ShotReport report = new ShotReport(this.shotResults);
		this.summary = report.summary();
		if (verbose) {
			this.summary = report.verboseMessage();
			System.out.println("Jogada nº" + this.number + " -> " + this.summary);
		}
		String jsonString = toJson(report);
		System.out.println(jsonString);
		System.out.println();
		return jsonString;
	}

	/**
	 * Builds the JSON with the results of the burst.
	 *
	 * @param report the counted results of the burst
	 * @return the JSON string (indented)
	 */
	private String toJson(ShotReport report) {
		Map<String, Object> response = new HashMap<>();
		response.put("validShots", report.validShots);
		response.put("outsideShots", report.outsideShots);
		response.put("repeatedShots", report.repeatedShots);
		response.put("missedShots", report.missedShots);
		response.put("sunkBoats", buildSunkBoatsList(report.sunkBoatsCount));
		response.put("hitsOnBoats", buildBoatHitsList(report));
		try {
			return objectMapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Erro ao serializar o JSON dos resultados da jogada", e);
		}
	}

	private static @NotNull List<Map<String, Object>> buildSunkBoatsList(Map<String, Integer> sunkBoatsCount) {
		List<Map<String, Object>> sunkBoats = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
			Map<String, Object> boat = new HashMap<>();
			boat.put("type", entry.getKey());
			boat.put("count", entry.getValue());
			sunkBoats.add(boat);
		}
		return sunkBoats;
	}

	private static @NotNull List<Map<String, Object>> buildBoatHitsList(ShotReport report) {
		List<Map<String, Object>> boatHits = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : report.hitsPerBoat.entrySet()) {
			if (!report.sunkBoatsCount.containsKey(entry.getKey())) {
				Map<String, Object> boat = new HashMap<>();
				boat.put("type", entry.getKey());
				boat.put("hits", entry.getValue());
				boatHits.add(boat);
			}
		}
		return boatHits;
	}

	/**
	 * Contagem e descrição dos resultados de uma rajada.
	 * Criada com Replace Method with Method Object a partir de processEnemyFire (Long Method):
	 * cada método fica com CC até 5, até 25 instruções e aninhamento até 3.
	 */
	private static final class ShotReport {

		int validShots;
		int repeatedShots;
		int missedShots;
		int outsideShots;
		final Map<String, Integer> sunkBoatsCount = new HashMap<>();
		final Map<String, Integer> hitsPerBoat = new HashMap<>();

		/**
		 * Conta os resultados de uma rajada.
		 *
		 * @param shotResults resultados dos tiros da rajada
		 */
		ShotReport(List<IGame.ShotResult> shotResults) {
			for (IGame.ShotResult result : shotResults) {
				if (result.valid()) {
					countValidResult(result);
				}
			}
			outsideShots = Game.NUMBER_SHOTS - validShots - repeatedShots;
		}

		private void countValidResult(IGame.ShotResult result) {
			if (result.repeated()) {
				repeatedShots++;
			} else {
				validShots++;
				if (result.ship() == null) {
					missedShots++;
				} else {
					countHit(result);
				}
			}
		}

		private void countHit(IGame.ShotResult result) {
			String boatName = result.ship().getCategory();
			hitsPerBoat.put(boatName, hitsPerBoat.getOrDefault(boatName, 0) + 1);
			if (result.sunk()) {
				sunkBoatsCount.put(boatName, sunkBoatsCount.getOrDefault(boatName, 0) + 1);
			}
		}

		/**
		 * "n singular" se n == 1, "n plural" caso contrário.
		 */
		static String plural(int n, String singular, String pluralForm) {
			return n + " " + (n == 1 ? singular : pluralForm);
		}

		// ---------- resumo para o diário de bordo (PDF) ----------

		/**
		 * Resumo curto da rajada, usado no diário de bordo.
		 *
		 * @return o resumo, por exemplo "2 Tiros na Água, 1 Tiro Exterior"
		 */
		String summary() {
			List<String> partes = new ArrayList<>();
			if (missedShots > 0) {
				partes.add(plural(missedShots, "Tiro na Água", "Tiros na Água"));
			}
			addHitAndSunkParts(partes);
			if (repeatedShots > 0) {
				partes.add(plural(repeatedShots, "Tiro Repetido", "Tiros Repetidos"));
			}
			if (outsideShots > 0) {
				partes.add(plural(outsideShots, "Tiro Exterior", "Tiros Exteriores"));
			}
			return partes.isEmpty() ? "Tiros Inválidos" : String.join(", ", partes);
		}

		private void addHitAndSunkParts(List<String> partes) {
			for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
				if (!sunkBoatsCount.containsKey(entry.getKey())) {
					partes.add(plural(entry.getValue(), "Tiro na " + entry.getKey(), "Tiros na " + entry.getKey()));
				}
			}
			for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
				partes.add(entry.getValue() + " " + entry.getKey() + " ao fundo");
			}
		}

		// ---------- mensagem detalhada (verbose) ----------

		/**
		 * Mensagem detalhada da rajada, mostrada na consola.
		 *
		 * @return a mensagem, por exemplo "3 tiros válidos: 1 Barca ao fundo + 2 tiros na água"
		 */
		String verboseMessage() {
			StringBuilder output = new StringBuilder();
			if (validShots == 0 && repeatedShots > 0) {
				output.append(plural(repeatedShots, "tiro repetido", "tiros repetidos"));
			} else {
				appendValidShots(output);
				appendRepeatedShots(output);
			}
			appendOutsideShots(output);
			return output.toString();
		}

		private void appendValidShots(StringBuilder output) {
			if (validShots > 0) {
				output.append(plural(validShots, "tiro válido", "tiros válidos")).append(": ");
			}
			appendSunkAndHits(output);
			if (missedShots > 0) {
				output.append(plural(missedShots, "tiro na água", "tiros na água"));
			} else if (!sunkBoatsCount.isEmpty() || !hitsPerBoat.isEmpty()) {
				output.setLength(output.length() - 2); // remove o "+" final
			}
		}

		private void appendSunkAndHits(StringBuilder output) {
			for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
				output.append(plural(entry.getValue(), entry.getKey(), entry.getKey() + "s")).append(" ao fundo + ");
			}
			for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
				if (!sunkBoatsCount.containsKey(entry.getKey())) {
					output.append(plural(entry.getValue(), "tiro", "tiros")).append(" num(a) ").append(entry.getKey()).append(" + ");
				}
			}
		}

		private void appendRepeatedShots(StringBuilder output) {
			if (repeatedShots > 0) {
				if (validShots > 0) {
					output.append(", ");
				}
				output.append(plural(repeatedShots, "tiro repetido", "tiros repetidos"));
			}
		}

		private void appendOutsideShots(StringBuilder output) {
			if (outsideShots > 0) {
				if (!output.isEmpty()) {
					output.append(", ");
				}
				output.append(plural(outsideShots, "tiro exterior", "tiros exteriores"));
			}
		}
	}
}