package battleship;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.apache.commons.lang3.time.StopWatch;
import java.util.concurrent.TimeUnit;

/**
 * The type Tasks.
 */
public class Tasks {
	/**
	 * The constant LOGGER.
	 */
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * The constant GOODBYE_MESSAGE.
	 */
	private static final String GOODBYE_MESSAGE = "Bons ventos!";

	/**
	 * Strings to be used by the user
	 */
	private static final String AJUDA = "ajuda";
	private static final String GERAFROTA = "gerafrota";
	private static final String LEFROTA = "lefrota";
	private static final String DESISTIR = "desisto";
	private static final String RAJADA = "rajada";
	private static final String TIROS = "tiros";
	private static final String MAPA = "mapa";
	private static final String STATUS = "estado";
	private static final String SIMULA = "simula";
	private static final String PDF_FILE = "jogadas.pdf";
	private static final String PDF = "pdf";
	private static final String SCOREBOARD = "scoreboard"; // Novo comando adicionado

	/**
	 * This task also tests the fighting element of a round of three shots
	 */
	public static void menu() {

		IFleet myFleet = null;
		IGame game = null;

		// Diário de bordo: uma linha por rajada, usada no PDF
		List<String> diarioDeBordo = new ArrayList<>();
		int numeroDeRajadas = 0;

		menuHelp();

		// 1. Cronómetro total da partida
		StopWatch tempoTotal = StopWatch.createStarted();
		// 2. Cronómetro individual por jogada
		StopWatch tempoJogada = new StopWatch();

		System.out.print("> ");
		Scanner in = new Scanner(System.in);

		// Inicia a contar o tempo que demoras a pensar e a digitar
		tempoJogada.start();
		String command = in.next();

		while (!command.equals(DESISTIR)) {

			switch (command) {
				case GERAFROTA:
					myFleet = Fleet.createRandom();
					game = new Game(myFleet);
					numeroDeRajadas = 0;      // novo jogo: reinicia o contador
					diarioDeBordo.clear();    // e o diário de bordo
					game.printMyBoard(false, true);
					break;
				case LEFROTA:
					myFleet = buildFleet(in);
					game = new Game(myFleet);
					numeroDeRajadas = 0;
					diarioDeBordo.clear();
					game.printMyBoard(false, true);
					break;
				case STATUS:
					if (myFleet != null)
						myFleet.printStatus();
					break;
				case MAPA:
					if (myFleet != null)
						game.printMyBoard(false, true);
					break;
				case RAJADA:
					if (game != null) {
						numeroDeRajadas++; // Incrementa as rajadas efetuadas
						game.readEnemyFire(in);

						// Pára o tempo assim que a rajada é lida e os tiros processados
						tempoJogada.stop();
						System.out.println("⏱️  Tempo desta jogada: " + tempoJogada.getTime(TimeUnit.SECONDS) + " segundo(s).\n");
						diarioDeBordo.add("Rajada " + numeroDeRajadas + " (Manual): " + resumoUltimaJogada(game));

						myFleet.printStatus();
						game.printMyBoard(true, false);

						if (game.getRemainingShips() == 0) {
							tempoTotal.stop();
							System.out.println("\n========================================");
							System.out.println("⏱️  Tempo total de jogo: " + tempoTotal.getTime(TimeUnit.SECONDS) + " segundo(s).");
							System.out.println("========================================");
							terminaJogo(game, diarioDeBordo);
						}
					}
					break;
				case SIMULA:
					if (game != null) {
						while (game.getRemainingShips() > 0){
							numeroDeRajadas++; // Incrementa as rajadas na simulação
							game.randomEnemyFire();
							diarioDeBordo.add("Rajada " + numeroDeRajadas + " (Automática): " + resumoUltimaJogada(game));

							myFleet.printStatus();
							game.printMyBoard(true, false);
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}
						}

						if (game.getRemainingShips() == 0) {
							tempoTotal.stop();
							System.out.println("\n========================================");
							System.out.println("⏱️  Tempo total de jogo (Simulação): " + tempoTotal.getTime(TimeUnit.SECONDS) + " segundo(s).");
							System.out.println("========================================");
              terminaJogo(game, diarioDeBordo);
						}
					}
					break;
				case TIROS:
					if (game != null)
						game.printMyBoard(true, true);
					break;
				case PDF:
					if (diarioDeBordo.isEmpty())
						System.out.println("Ainda não há rajadas para exportar.");
					else
						PdfExporter.exportShots(diarioDeBordo, PDF_FILE);
          break;
				case SCOREBOARD:
					Scoreboard.showScoreboard(); // Exibe os resultados anteriores
					break;
				case AJUDA:
					menuHelp();
					break;
				default:
					System.out.println("Que comando é esse??? Repete ...");
			}

			// Reinicia o cronómetro para voltar a medir a PRÓXIMA jogada
			tempoJogada.reset();
			System.out.print("> ");
			tempoJogada.start();
			command = in.next();
		}

		// Parar o cronómetro total caso o utilizador digite "desisto"
		tempoTotal.stop();
		System.out.println("\n========================================");
		System.out.println("⏱️  Tempo total de jogo: " + tempoTotal.getTime(TimeUnit.SECONDS) + " segundo(s).");
		System.out.println("========================================");
		// O jogador desistiu: exporta o PDF das rajadas já feitas
		if (!diarioDeBordo.isEmpty()) {
			PdfExporter.exportShots(diarioDeBordo, PDF_FILE);
		}
		System.out.println(GOODBYE_MESSAGE);
	}
	private static void terminaJogo(IGame game, List<String> diarioDeBordo) {
		game.over();
		PdfExporter.exportShots(diarioDeBordo, PDF_FILE);
		System.exit(0);
	}

	private static String resumoUltimaJogada(IGame game) {
		List<IMove> jogadas = game.getAlienMoves();
		return jogadas.isEmpty() ? "" : jogadas.get(jogadas.size() - 1).getSummary();
	}

	public static void menuHelp() {
		System.out.println("======================= AJUDA DO MENU =========================");
		System.out.println("Digite um dos comandos abaixo para interagir com o jogo:");
		System.out.println("- " + GERAFROTA + ": Gera uma frota aleatória de navios.");
		System.out.println("- " + LEFROTA + ": Permite criar e carregar uma frota personalizada.");
		System.out.println("- " + STATUS + ": Mostra o status atual da frota.)");
		System.out.println("- " + MAPA + ": Exibe o mapa da frota.");
		System.out.println("- " + RAJADA + ": Realiza uma rajada de disparos.");
		System.out.println("- " + SIMULA + ": Simula um jogo completo.");
		System.out.println("- " + TIROS + ": Lista os tiros válidos realizados (* = tiro em navio, o = tiro na água)");
		System.out.println("- " + PDF + ": Exporta o diário de bordo das rajadas para " + PDF_FILE + ".");
		System.out.println("- " + SCOREBOARD + ": Consulta o histórico de jogos passados (rajadas/data).");
		System.out.println("- " + DESISTIR + ": Encerra o jogo.");
		System.out.println("===============================================================");
	}

	/**
	 * This operation allows the build up of a fleet, given user data
	 *
	 * @param in The scanner to read from
	 * @return The fleet that has been built
	 */
	public static Fleet buildFleet(Scanner in) {
		assert in != null;
		Fleet fleet = new Fleet();
		int i = 0;
		while (i < Fleet.FLEET_SIZE) {
			IShip s = readShip(in);
			if (s != null) {
				boolean success = fleet.addShip(s);
				if (success)
					i++;
				else
					LOGGER.info("Falha na criacao de {} {} {}", s.getCategory(), s.getBearing(), s.getPosition());
			} else {
				LOGGER.info("Navio desconhecido!");
			}
		}
		LOGGER.info("{} navios adicionados com sucesso!", i);
		return fleet;
	}

	public static Ship readShip(Scanner in) {
		assert in != null;
		String shipKind = in.next();
		Position pos = readPosition(in);
		char c = in.next().charAt(0);
		Compass bearing = Compass.charToCompass(c);
		return Ship.buildShip(shipKind, bearing, pos);
	}

	public static Position readPosition(Scanner in) {
		assert in != null;
		int row = in.nextInt();
		int column = in.nextInt();
		return new Position(row, column);
	}

	public static IPosition readClassicPosition(@NotNull Scanner in) {
		if (!in.hasNext()) {
			throw new IllegalArgumentException("Nenhuma posição válida encontrada!");
		}

		String part1 = in.next();
		String part2 = null;

		if (in.hasNextInt()) {
			part2 = in.next();
		}

		String input = (part2 != null) ? part1 + part2 : part1;
		input = input.toUpperCase();

		if (input.matches("[A-Z]\\d+")) {
			char column = input.charAt(0);
			int row = Integer.parseInt(input.substring(1));
			return new Position(column, row);
		} else if (part2 != null && part1.matches("[A-Z]") && part2.matches("\\d+")) {
			char column = part1.charAt(0);
			int row = Integer.parseInt(part2);
			return new Position(column, row);
		} else {
			throw new IllegalArgumentException("Formato inválido. Use 'A3', 'A 3' ou similar.");
		}
	}
}
