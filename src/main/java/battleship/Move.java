package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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

	/**
	 * Processes the results of enemy fire on the game board, analyzing the outcomes of shots,
	 * such as valid shots, repeated shots, missed shots, hits on ships, and sunk ships. It can
	 * also display a detailed summary of the shot results if verbose mode is activated.
	 *
	 * @param verbose a boolean indicating whether a detailed summary should be printed to the console
	 *                for the processed enemy fire data.
	 * @return a JSON-formatted string that encapsulates the results, including counts of valid shots,
	 *         repeated shots, missed shots, shots outside the game board, and details of hits and
	 *         sunk ships.
	 */
	@Override
	public String processEnemyFire(boolean verbose) {

		int validShots = 0;
		int repeatedShots = 0;
		int missedShots = 0;

		Map<String, Integer> sunkBoatsCount = new HashMap<>();
		Map<String, Integer> hitsPerBoat = new HashMap<>();

		// Processar cada resultado de tiro
		for (IGame.ShotResult result : this.shotResults) {
			if (!result.valid()) {
				continue;
			}

			if (result.repeated())
				repeatedShots++;
			else {
				validShots++;
				if (result.ship() == null)
					missedShots++;
				else{
					String boatName = result.ship().getCategory();
					hitsPerBoat.put(boatName, hitsPerBoat.getOrDefault(boatName, 0) + 1);
					if (result.sunk())
						sunkBoatsCount.put(boatName, sunkBoatsCount.getOrDefault(boatName, 0) + 1);
				}
			}
		}

		int outsideShots = Game.NUMBER_SHOTS - validShots - repeatedShots;

		// 1. CONSTRUIR O RESUMO EXATAMENTE NO FORMATO PEDIDO
		List<String> partes = new ArrayList<>();
		if (missedShots > 0) {
			partes.add(missedShots + (missedShots == 1 ? " Tiro na Água" : " Tiros na Água"));
		}
		for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
			if (!sunkBoatsCount.containsKey(entry.getKey())) {
				partes.add(entry.getValue() + (entry.getValue() == 1 ? " Tiro na " : " Tiros na ") + entry.getKey());
			}
		}
		for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
			partes.add(entry.getValue() + " " + entry.getKey() + " ao fundo");
		}
		if (repeatedShots > 0) {
			partes.add(repeatedShots + (repeatedShots == 1 ? " Tiro Repetido" : " Tiros Repetidos"));
		}
		if (outsideShots > 0) {
			partes.add(outsideShots + (outsideShots == 1 ? " Tiro Exterior" : " Tiros Exteriores"));
		}

		String textoResumo = String.join(", ", partes);
		if (textoResumo.isEmpty()) {
			textoResumo = "Tiros Inválidos";
		}

		if (verbose) {
			System.out.println("Jogada nº" + this.number + " -> " + textoResumo);
		}

		// 2. CRIAR O JSON (Para a Ficha - Parte C)
		Map<String, Object> response = new HashMap<>();
		response.put("validShots", validShots);
		response.put("outsideShots", outsideShots);
		response.put("repeatedShots", repeatedShots);
		response.put("missedShots", missedShots);

		List<Map<String, Object>> sunkBoats = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
			Map<String, Object> boat = new HashMap<>();
			boat.put("type", entry.getKey());
			boat.put("count", entry.getValue());
			sunkBoats.add(boat);
		}
		response.put("sunkBoats", sunkBoats);

		List<Map<String, Object>> boatHits = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
			if (!sunkBoatsCount.containsKey(entry.getKey())) {
				Map<String, Object> boat = new HashMap<>();
				boat.put("type", entry.getKey());
				boat.put("hits", entry.getValue());
				boatHits.add(boat);
			}
		}
		response.put("hitsOnBoats", boatHits);

		String jsonString;
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		try {
			jsonString = objectMapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Erro ao serializar o JSON", e);
		}

		// 3. Imprime o JSON na consola APENAS na chamada principal (verbose = true)
		if (verbose) {
			System.out.println(jsonString);
			System.out.println();
		}

		// 4. RETORNAR APENAS O TEXTO PARA O PDF
		return textoResumo;
	}
}