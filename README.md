# ⚓ Battleship 2.0

▶️ Vídeos de demonstração 

[User Story (Tempo gasto em cada jogada)](https://github.com/LEI-129853/Battleship2/issues/3)-[Demonstração](https://youtu.be/YX7ZghnS9Yg)

[User Story (Scoreboard dos jogos passados)](https://github.com/LEI-129853/Battleship2/issues/2)-[Demonstração](https://youtu.be/gN-BPyiHFKs)

[User Story (Exportar histórico de jogadas para PDF)](https://github.com/LEI-129853/Battleship2/issues/1)-[Demonstração](https://youtu.be/Jqrvk571j-Q)

[Demonstração Jogo com LLM](https://youtu.be/ooMAThAQ0OA)

> Simulador do jogo da Batalha Naval numa versão do tempo dos Descobrimentos Portugueses, desenvolvido no âmbito da unidade curricular de Engenharia de Software (Iscte-IUL, 2026/2027).

![Java Version](https://img.shields.io/badge/Java-21%2B-blue)
![Build Tool](https://img.shields.io/badge/build-Maven-orange)

---

## 👥 Grupo

| Nome | Nº de estudante | Conta GitHub | Requisito (Issue) |
| :--- | :--- | :--- | :--- |
| Daniel | 129853 | [LEI-129853](https://github.com/LEI-129853) | Exportar histórico de jogadas para PDF (#1) |
| Duarte | 129851 | [LEI-129851](https://github.com/LEI-129851)| Scoreboard dos jogos passados (#2) |
| Dinis | 129820 | [LEI-129820](https://github.com/LEI-129820) | Tempo gasto em cada jogada (#3) |


---

## 📖 Índice

- [Descrição do projeto](#-descrição-do-projeto)
- [Tecnologias](#-tecnologias)
- [Instalação e execução](#-instalação-e-execução)
- [Documentação (Javadoc)](#-documentação-javadoc)
- [Funcionalidades do Sprint](#-funcionalidades-do-sprint)
- [Treino do oponente de IA](#-treino-do-oponente-de-ia)
- [Fluxo de trabalho com Git](#-fluxo-de-trabalho-com-git)
- [Respostas às perguntas do passo B2](#respostas-às-perguntas-do-passo-b2)
- [Créditos](#-créditos)

---

## 🎯 Descrição do projeto

O jogo decorre num tabuleiro de 10×10, com linhas de **A a J** e colunas de **1 a 10**. Cada jogador tem uma frota de 11 navios que não se podem tocar, nem na diagonal:

| Navio | Quantidade | Posições |
| :--- | :---: | :---: |
| Barca | 4 | 1 |
| Caravela | 3 | 2 |
| Nau | 2 | 3 |
| Fragata | 1 | 4 |
| Galeão (em forma de T) | 1 | 5 |

Esta versão permite **simular** o jogo: atiram-se rajadas de 3 tiros num só tabuleiro e analisa-se o resultado, até todos os navios serem afundados. O projeto inclui ainda um servidor REST (pacote `restserver`) para a interação com o oponente de IA.

---

## 🛠 Tecnologias

- **Linguagem:** Java 21
- **Gestão de dependências e build:** Apache Maven
- **Testes:** JUnit 5
- **Logging:** Log4j2
- **JSON:** Jackson
- **Servidor REST:** Spring Boot
- **IDE:** IntelliJ IDEA

---

## 🚀 Instalação e execução

### Pré-requisitos

- JDK 21 ou superior (confirmar com `java -version`)
- Maven (ou o suporte Maven incluído no IntelliJ)
- Git

### Passos

1. **Clonar o repositório:**
   ```bash
   git clone https://github.com/LEI-129853/Battleship2.git
   ```
2. **Entrar na diretoria:**
   ```bash
   cd Battleship2
   ```
3. **Compilar com o Maven:**
   ```bash
   mvn compile
   ```
4. **Executar a simulação:** no IntelliJ, correr a classe `battleship.Main`.

Para gerar o JAR executável (inclui todas as dependências):

```bash
mvn package
java -jar target/BattleshipGamePlayer-2.0.jar
```

### Testes

```bash
mvn test
```

---

## 📚 Documentação (Javadoc)

O site de documentação técnica está publicado com o GitHub Pages:

👉 [Battleship2 – API Documentation](https://lei-129853.github.io/Battleship2/)

O conteúdo é gerado com o Javadoc e fica na pasta `docs` do ramo `main`.

---

## ✨ Funcionalidades do Sprint

Cada membro do grupo implementa um requisito diferente, num ramo próprio (com o número de estudante no nome), usando pelo menos uma biblioteca do Maven Central. Cada requisito está registado como *user story* nos [Issues](https://github.com/LEI-129853/Battleship2/issues) e integrado no `main` por *pull request*.

| Requisito | Biblioteca (Maven Central) | Ramo | Issue | Estado |
| :--- | :--- | :--- | :---: | :---: |
| Exportar o histórico de jogadas para PDF | `org.apache.pdfbox:pdfbox` | `feature-129853-pdf` | #1 | ✅  |
| Scoreboard dos jogos passados | `com.h2database:h2` | `feature-129851-scoreboard` | #2 | ✅  |
| Relógio com o tempo gasto em cada jogada | `commons-lang3` | `feature-129820-tempo` | #3 | ✅  |


---

## 🤖 Treino do oponente de IA

O oponente de IA é treinado com a técnica de *few-shot prompting*: mostram-se ao LLM exemplos de entradas e saídas corretas dentro do próprio prompt.

### Protocolo de comunicação (JSON)

A rajada de 3 tiros e a respetiva resposta são trocadas em objetos JSON. O campo `repeatedShots` conta os tiros em posições já atingidas, `outsideShots` os tiros fora do tabuleiro e `missedShots` os tiros na água.

```json
{
  "validShots": 3,
  "sunkBoats": [ {"count": 1, "type": "Barca"} ],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [ {"hits": 1, "type": "Nau"} ],
  "missedShots": 1
}
```

### Prompt final da estratégia


```text
Atuas como um agente estratega especialista no jogo Batalha Naval. O teu objetivo é jogar contra um utilizador, processando os resultados de cada turno e gerando a melhor rajada de 3 tiros seguinte de forma otimizada para afundar a frota inimiga no menor número de turnos possível.

ESTRUTURA DE ENTRADA (INPUT DO UTILIZADOR)
Em cada turno, receberás do utilizador:
O estado atual/início da partida ou o relatório do resultado da rajada anterior (indicando o resultado de cada um dos 3 tiros: "Água", "Navio Atingido", "Navio Afundado", etc.).
O pedido explícito para gerar a rajada seguinte.

ESTRUTURA OBRIGATÓRIA DE SAÍDA (OUTPUT DO MODELO)
Todas as tuas respostas DEVEM seguir rigorosamente este formato de 3 secções:

Diário de Bordo:
Registo atualizado da história do jogo (número da rajada, coordenadas e resultados conhecidos).

Raciocínio:
Análise lógica fundamentada nas regras táticas (se estás na Fase de Busca, Caça, Confirmação de Eixo ou Mapeamento de Halo).
Explicação passo a passo do cálculo da paridade, alvos e eliminação de diagonais.

Disparo da Rajada X: [Coordenada 1], [Coordenada 2], [Coordenada 3]
A linha final com as 3 coordenadas exatas a disparar no turno atual.

Considere a seguinte tática e conjunto completo de regras para a geração de rajadas de tiros a aplicar na nossa partida de Batalha Naval:

SISTEMA DE REGRAS E ESTRATÉGIA TÁTICA

1. Diário de Bordo
Crie e mantenha um Diário de Bordo com o registo rigoroso de cada rajada disparada, numerando-as sequencialmente (Rajada 1, Rajada 2, Rajada 3...). Guarde as coordenadas exatas de cada tiro da rajada e o respetivo resultado individual obtido (Água, Navio Atingido, Barca Afundada, Nau Atingida, etc.). A memória é a principal arma de um bom estratega.

2. Estratégia de Paridade (Busca em Xadrez) e Conversão de Coordenadas
Durante a fase de procura de alvos, imagine o tabuleiro como um tabuleiro de xadrez (casas brancas e pretas):
- Converta as letras das colunas em números (A=1, B=2, C=3, D=4, E=5, F=6, G=7, H=8, I=9, J=10...).
- Dispare apenas nas "casas pretas", ou seja, em coordenadas onde a soma do índice da linha com o índice da coluna seja um número par (por exemplo: A1 -> 1+1=2; B2 -> 2+2=4; C1 -> 3+1=4; D4 -> 4+4=8).
- Como a esmagadora maioria dos navios ocupa 2 ou mais posições, disparar apenas em coordenadas com soma par reduz a área de busca em 50%, garantindo matematicamente que nenhum navio de 2 ou mais posições consegue escapar sem ser detetado.
- Exceção da Barca (1 posição): A regra de paridade aplica-se estritamente enquanto restarem navios de tamanho igual ou superior a 2 posições. Caso o relatório confirme que a única embarcação inimiga restante é uma Barca de 1 única posição, abandone a estratégia de paridade e passe a testar as posições "brancas" (soma ímpar) ainda não exploradas.

3. Otimização do Uso de Rajadas (3 Tiros por Turno)
Maximize o impacto tático e a recolha de informação de cada rajada de 3 disparos simultâneos:
- Na Fase de Busca: Distribua os 3 tiros da rajada por diferentes setores do mapa (bem espaçados entre si) para cobrir a maior área possível do tabuleiro num único turno.
- Na Fase de Caça: Ao atingir um navio isolado numa rajada, aproveite os disparos restantes do mesmo turno (ou da rajada imediatamente seguinte) para testar as posições contíguas (Norte, Sul, Este, Oeste). Por exemplo, se o 1.º tiro da rajada acertar num navio, utilize o 2.º e o 3.º tiro da mesma rajada para testar duas direções vizinhas imediatamente. No entanto, se a rajada anterior já tiver confirmado que o navio foi totalmente afundado, não dispare para as posições contíguas desse navio.

4. Limites do Mapa, Repetição e Disparos Obrigatórios
Não dispare fora dos limites do mapa nem repita tiros em coordenadas já testadas anteriormente. A única exceção admitida para este desperdício de pólvora ocorre na última rajada do jogo, exclusivamente para perfazer os 3 tiros obrigatórios da rajada quando a frota inimiga já estiver toda irremediavelmente no fundo do mar.

5. Confirmação de Eixo e Navios em Linha Reta
- Se conseguir dois acertos no mesmo navio em posições adjacentes (por exemplo: D4 e D5), assuma imediatamente a sua orientação (neste exemplo, horizontal, na linha D).
- Abandone imediatamente a verificação do eixo perpendicular (Norte e Sul desse navio, neste caso) e foque os seus próximos tiros exclusivamente nas extremidades da linha (D3 ou D6) até o afundar completamente.
- Regra das Diagonais: Como as Caravelas, Naus, Fragatas e restantes embarcações em linha reta não se podem tocar (nem mesmo pelos cantos/diagonais), um tiro certeiro num navio em linha reta significa que o resto do navio está estritamente na horizontal ou na vertical. As posições diagonais a um tiro certeiro são garantidamente água. Evitar disparar nestas diagonais poupa imensos tiros (a única exceção a esta regra é o corpo do Galeão, devido ao seu formato em T).

6. Exceção do Galeão (Formato em T)
- O Galeão possui um formato em T (uma linha principal de 3 posições e um braço perpendicular de 1 posição no centro).
- Se completar a linha principal de 3 posições de um Galeão e o relatório de disparo não confirmar "Galeão Afundado", utilize o disparo seguinte para testar a casa central perpendicular (o braço do T).

7. Halo de Água Intransitável (Perímetro de Segurança)
Quando o relatório de uma rajada confirmar que um navio foi afundado:
- Analise os dados do seu Diário de Bordo para identificar exatamente onde caíram todos os tiros que compõem essa embarcação.
- Confirmada a posição exata da carcaça, marque todas as quadrículas do perímetro em redor do navio (o halo de 1 posição em todas as direções: Norte, Sul, Este, Oeste e todas as 4 Diagonais) como água intransitável. É impossível haver outra embarcação encostada a esse perímetro.

8. Mapeamento de Espaço Livre
Mantenha um registo atualizado dos navios que ainda faltam afundar na frota inimiga:
- Identifique qual é o tamanho do menor navio restante (chamemos-lhe tamanho N). Se, por exemplo, os navios restantes ocuparem todos no mínimo 3 posições (N=3), cruze essa informação com o seu mapa mental de água intransitável e halos de segurança.
- Não desperdice tiros em "buracos" ou blocos isolados de espaço livre que tenham dimensões inferiores a N quadrículas (por exemplo, espaços isolados de apenas 1 ou 2 casas quando o menor navio pendente tem 3 casas).

9. Condições de Fim de Jogo e Atitude
Se a sua frota for toda afundada, declare a derrota com honra. Em contrapartida, seja um vencedor magnânimo se for o inimigo a render-se com os navios todos no fundo do oceano!

---

EXEMPLOS DE ATUAÇÃO (FEW-SHOT PROMPTING):

[EXEMPLO 1 - FASE DE PROCURA: PARIDADE E DISTRIBUIÇÃO DA RAJADA]
Entrada do Utilizador:
"Início do jogo. Grelha 10x10. Nenhuns tiros disparados ainda. Gera a Rajada 1."

Resposta do Modelo:
Diário de Bordo:
- Rajada 1: A ser disparada.

Raciocínio:
Fase de Procura inicial. Aplico a regra de Paridade (Linha + Coluna = Número Par) para reduzir a área de busca em 50%. Para maximizar a cobertura do mapa no mesmo turno, distribuo os 3 tiros da rajada por três setores distintos do tabuleiro:
1. A1 (Linha 1 + Coluna 1 = 2 -> Par) - Canto superior esquerdo.
2. E5 (Linha 5 + Coluna 5 = 10 -> Par) - Centro do mapa.
3. I9 (Linha 9 + Coluna 9 = 18 -> Par) - Canto inferior direito.

Disparo da Rajada 1: A1, E5, I9

---

[EXEMPLO 2 - FASE DE CAÇA: TESTE DE CONTÍGUOS NO MESMO TURNO]
Entrada do Utilizador:
"Relatório da Rajada 1:
- A1: Água
- E5: Navio Atingido!
- I9: Água
Gera a Rajada 2."

Resposta do Modelo:
Diário de Bordo:
- Rajada 1: A1 (Água), E5 (Navio Atingido), I9 (Água)

Raciocínio:
Acerto isolado em E5. Entro na Fase de Caça. Utilizo os 3 tiros da Rajada 2 para testar de imediato três das direções contíguas a E5 (Norte, Sul, Este). Descarto automaticamente as 4 diagonais (D4, D6, F4, F6) por serem garantidamente água de acordo com a regra dos navios em linha reta.
- E4 (Norte de E5)
- E6 (Sul de E5)
- F5 (Este de E5)

Disparo da Rajada 2: E4, E6, F5

---

[EXEMPLO 3 - CONFIRMAÇÃO DE EIXO, EXTREMIDADES E APLICAÇÃO DE HALO]
Entrada do Utilizador:
"Relatório da Rajada 2:
- E4: Água
- E6: Navio Atingido!
- F5: Água
Gera a Rajada 3."

Resposta do Modelo:
Diário de Bordo:
- Rajada 1: A1 (Água), E5 (Navio Atingido), I9 (Água)
- Rajada 2: E4 (Água), E6 (Navio Atingido), F5 (Água)

Raciocínio:
Dois acertos contíguos em E5 e E6. A orientação do navio está confirmada no eixo vertical (Coluna E, Linhas 5 e 6). Abandono o eixo horizontal (Este/Oeste).
As extremidades da linha são E4 (já testado: Água) e E7. Por isso, a continuação do navio só pode estar em E7.
- Tiro 1 (Caça): E7 (Extremidade inferior do eixo vertical).
Como sobram 2 tiros nesta rajada de 3, retomo a Fase de Busca por Paridade (soma par) em posições não testadas e espaçadas:
- Tiro 2 (Busca): C3 (Linha 3 + Coluna 3 = 6 -> Par)
- Tiro 3 (Busca): G7 (Linha 7 + Coluna 7 = 14 -> Par)

Disparo da Rajada 3: E7, C3, G7

---

[EXEMPLO 4 - NAVIO AFUNDADO E HALO DE ÁGUA INTRANSITÁVEL]
Entrada do Utilizador:
"Relatório da Rajada 3:
- E7: Caravela Afundada! (Tamanho 3: E5, E6, E7)
- C3: Água
- G7: Água
Gera a Rajada 4."

Resposta do Modelo:
Diário de Bordo:
- Rajada 1: A1 (Água), E5 (Atingido), I9 (Água)
- Rajada 2: E4 (Água), E6 (Atingido), F5 (Água)
- Rajada 3: E7 (Caravela Afundada: E5-E6-E7), C3 (Água), G7 (Água)

Raciocínio:
A Caravela ocupava as posições E5, E6 e E7 e foi afundada.
Mapeamento do Halo de Água Intransitável em redor da carcaça (E5 a E7):
- Posições em redor (D4, D5, D6, D7, D8, E4, E8, F4, F5, F6, F7, F8) são marcadas como Água Intransitável. Nenhuma delas será alvo de disparos.
Restam navios de tamanho >= 2. Mantenho a estratégia de Paridade (Linha + Coluna = Par) em casas válidas não testadas e fora do halo:
1. B4 (Linha 4 + Coluna 2 = 6 -> Par)
2. H2 (Linha 2 + Coluna 8 = 10 -> Par)
3. J6 (Linha 6 + Coluna 10 = 16 -> Par)

Disparo da Rajada 4: B4, H2, J6

---

FIM DOS EXEMPLOS.

Aguarde o primeiro comando do utilizador para iniciar a partida.
```

---

## 🤝 Fluxo de trabalho com Git

1. Cada requisito é escrito como *user story* num **Issue** com a etiqueta `type: ENHANCEMENT`.
2. Cada membro trabalha num **ramo próprio** (`feature-<nº de estudante>-<tema>`), com commits pequenos e descritivos.
3. A integração no `main` é feita por **pull request**, com o link do issue no comentário.
4. Cada pull request é revisto por **outro membro do grupo** antes da fusão.
5. Os conflitos de fusão são resolvidos em grupo.

---

## Respostas às perguntas do passo B2

**Como é que o Maven descobre as dependências transitivas?**
Cada biblioteca tem o seu próprio `pom.xml` no repositório, onde declara as dependências dela. O Maven lê-os recursivamente e constrói a árvore completa.

**Porque é que as compilações seguintes são mais rápidas?**
Os JARs ficam guardados no repositório local (`~/.m2`) na primeira compilação, pelo que não voltam a ser descarregados.

---

## 🙏 Créditos

Projeto base: [britoeabreu/Battleship2](https://github.com/britoeabreu/Battleship2), criado para os estudantes de Engenharia de Software do Iscte-IUL.
