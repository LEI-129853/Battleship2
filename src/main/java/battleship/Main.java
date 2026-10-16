package battleship;

import org.apache.commons.lang3.time.StopWatch;
import java.util.concurrent.TimeUnit;

public class Main {

	/**
	 * Main.
	 *
	 * @param args the args
	 */
	public static void main(String[] args) {
		// Início da medição do tempo total da partida
		StopWatch tempoTotalJogo = StopWatch.createStarted();

		System.out.println("***  Battleship  ***");

		Tasks.menu();

		// Fim do jogo: apresenta o tempo acumulado
		tempoTotalJogo.stop();
		long segundosTotais = tempoTotalJogo.getTime(TimeUnit.SECONDS);

		System.out.println("\n========================================");
		System.out.println("⏱️  Tempo total de jogo: " + segundosTotais + " segundo(s).");
		System.out.println("========================================");
	}
}