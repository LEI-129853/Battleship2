package battleship;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
	public static final String CLASSIC_POSITION_REGEX = "[A-Z]\\d+";

	/**
	 * This task also tests the fighting element of a round of three shots.
	 * The game loop is implemented by {@link GameMenu} (Replace Method with Method Object):
	 * each command is handled by its own small method instead of one long switch.
	 */
	public static void menu() {
		new GameMenu(new Scanner(System.in)).run();
	}

	private static void imprimeTempoTotal(StopWatch tempoTotal, String modo) {
		tempoTotal.stop();
		System.out.println("\n========================================");
		System.out.println("⏱️  Tempo total de jogo" + modo + ": " + tempoTotal.getTime(TimeUnit.SECONDS) + " segundo(s).");
		System.out.println("========================================");
	}

	private static void terminaJogo(IGame game, List<String> diarioDeBordo, int numeroDeRajadas) {
		game.over();
		Scoreboard.saveGame(numeroDeRajadas); // Grava no Scoreboard!
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
		String input = (in.hasNextInt() ? part1 + in.next() : part1).toUpperCase();

		if (input.matches(CLASSIC_POSITION_REGEX)) {
			char column = input.charAt(0);
			int row = Integer.parseInt(input.substring(1));
			return new Position(column, row);
		}
		throw new IllegalArgumentException("Formato inválido. Use 'A3', 'A 3' ou similar.");
	}

	/**
	 * Estado e comandos de uma sessão do menu do jogo.
	 * Criada com Replace Method with Method Object a partir de Tasks::menu (Long Method / Switch Statements):
	 * o switch foi substituído por um mapa comando - ação e cada comando tem o seu método.
	 */
	private static final class GameMenu {

		private final Scanner in;
		private final StopWatch tempoTotal = new StopWatch();
		private final StopWatch tempoJogada = new StopWatch();
		private final List<String> diarioDeBordo = new ArrayList<>();
		private final Map<String, Runnable> commands = new HashMap<>();
		private IFleet myFleet;
		private IGame game;
		private int numeroDeRajadas;

		GameMenu(Scanner in) {
			this.in = in;
			commands.put(GERAFROTA, () -> novoJogo(Fleet.createRandom()));
			commands.put(LEFROTA, () -> novoJogo(buildFleet(this.in)));
			commands.put(STATUS, this::estado);
			commands.put(MAPA, this::mapa);
			commands.put(RAJADA, this::rajada);
			commands.put(SIMULA, this::simula);
			commands.put(TIROS, this::tiros);
			commands.put(PDF, this::exportarPdf);
			commands.put(SCOREBOARD, Scoreboard::showScoreboard);
			commands.put(AJUDA, Tasks::menuHelp);
		}

		void run() {
			menuHelp();
			tempoTotal.start(); // 1. Cronómetro total da partida
			String command = proximoComando();
			while (!command.equals(DESISTIR)) {
				commands.getOrDefault(command, GameMenu::comandoDesconhecido).run();
				tempoJogada.reset(); // volta a medir a PRÓXIMA jogada
				command = proximoComando();
			}
			desistir();
		}

		private String proximoComando() {
			System.out.print("> ");
			tempoJogada.start(); // 2. Cronómetro individual por jogada
			return in.next();
		}

		private void desistir() {
			imprimeTempoTotal(tempoTotal, "");
			// O jogador desistiu: exporta o PDF das rajadas já feitas
			if (!diarioDeBordo.isEmpty()) {
				PdfExporter.exportShots(diarioDeBordo, PDF_FILE);
			}
			System.out.println(GOODBYE_MESSAGE);
		}

		private void novoJogo(IFleet fleet) {
			myFleet = fleet;
			game = new Game(myFleet);
			numeroDeRajadas = 0;      // novo jogo: reinicia o contador
			diarioDeBordo.clear();    // e o diário de bordo
			game.printMyBoard(false, true);
		}

		private void estado() {
			if (myFleet != null)
				myFleet.printStatus();
		}

		private void mapa() {
			if (myFleet != null)
				game.printMyBoard(false, true);
		}

		private void tiros() {
			if (game != null)
				game.printMyBoard(true, true);
		}

		private void exportarPdf() {
			if (diarioDeBordo.isEmpty())
				System.out.println("Ainda não há rajadas para exportar.");
			else
				PdfExporter.exportShots(diarioDeBordo, PDF_FILE);
		}

		private void rajada() {
			if (game == null)
				return;
			numeroDeRajadas++; // Incrementa as rajadas efetuadas
			game.readEnemyFire(in);
			// Pára o tempo assim que a rajada é lida e os tiros processados
			tempoJogada.stop();
			long segundosJogada = tempoJogada.getTime(TimeUnit.SECONDS);
			System.out.println("⏱️  Tempo desta jogada: " + segundosJogada + " segundo(s).\n");
			registarRajada("Manual");
			if (game.getRemainingShips() == 0) {
				imprimeTempoTotal(tempoTotal, "");
				terminaJogo(game, diarioDeBordo, numeroDeRajadas);
			}
		}

		private void simula() {
			if (game == null)
				return;
			while (game.getRemainingShips() > 0) {
				numeroDeRajadas++; // Incrementa as rajadas na simulação
				game.randomEnemyFire();
				registarRajada("Automática");
				pausa();
			}
			imprimeTempoTotal(tempoTotal, " (Simulação)");
			terminaJogo(game, diarioDeBordo, numeroDeRajadas);
		}

		private void registarRajada(String modo) {
			diarioDeBordo.add("Rajada " + numeroDeRajadas + " (" + modo + "): " + resumoUltimaJogada(game));
			myFleet.printStatus();
			game.printMyBoard(true, false);
		}

		private static void pausa() {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		private static void comandoDesconhecido() {
			System.out.println("Que comando é esse??? Repete ...");
		}
	}
}