# Oportunidades de refabricação — BattleShip2

Identificadas em conjunto pelo grupo (Daniel 129853, Dinis 129820, Duarte 129851) com o Qodana
(gates da ficha 3 e plugin), o SonarQube for IDE e revisão direta do código.
Cada membro aplica as refabricações das suas linhas no ramo `Refactoring_<nº>`, com a bateria de
testes a passar antes e depois de cada uma, e um commit por refabricação.

| Local | Cheiro no código | Refabricação (IntelliJ) | Nº aluno |
|---|---|---|---|
| Move::processEnemyFire (criação da lista `sunkBoats` para o JSON) | Long Method | Extract Method | 129853 |
| PdfExporter::sanitize (literais `32` e `255`) | Magic Number | Introduce Constant | 129853 |
| PdfExporter::exportShots (variável `cs` e parâmetro `linhas`) | Nomes pouco expressivos | Rename | 129853 |
| Move::processEnemyFire (`entry.getValue()` repetido no ciclo dos acertos) | Expressão duplicada | Introduce Variable | 129853 |
| Move::processEnemyFire (`ObjectMapper` criado em cada chamada) | Objeto recriado / Long Method | Introduce Field | 129853 |
| Ship (atributo `protected List<IPosition> positions`) | Exposição do estado interno (Encapsulation) | Encapsulate Fields | 129853 |
| Tasks::menu (bloco "Tempo total de jogo" repetido) | Duplicated Code / Long Method | Extract Method | 129820 |
| Tasks::readClassicPosition (expressão regular `"[A-Z]\\d+"`) | Magic String | Introduce Constant | 129820 |
| Fleet::colisionRisk (erro ortográfico no nome) | Nome incorreto | Rename | 129820 |
| Fleet::printShips (não usa o estado do objeto) | Feature Envy (MethodMayBeStatic) | Make Static | 129820 |
| Tasks::menu, caso RAJADA (`tempoJogada.getTime(TimeUnit.SECONDS)` dentro da concatenação) | Expressão complexa | Introduce Variable | 129820 |
| Scoreboard (`DriverManager.getConnection(DB_URL, USER, PASS)` repetido 3×) | Duplicated Code | Extract Method | 129851 |
| Scoreboard::saveGame (`DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")`) | Magic String / objeto recriado | Introduce Constant | 129851 |
| Game::printBoard, printMyBoard, printAlienBoard (parâmetros `show_shots`, `show_legend`) | Convenção de nomes (snake_case) | Rename | 129851 |
| Game::fireSingleShot (`!ship.stillFloating()` avaliado 2×) | Expressão duplicada | Introduce Variable | 129851 |
| Game::getRemainingShips (variável local `floatingShips` usada uma vez) | Variável redundante | Inline Variable | 129851 |
