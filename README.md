# ⚓ Battleship 2.0

▶️ **Vídeo de demonstração (máx. 5 min + 1 min com o LLM):** [COLAR AQUI O LINK DO YOUTUBE](https://www.youtube.com/)

> Simulador do jogo da Batalha Naval numa versão do tempo dos Descobrimentos Portugueses, desenvolvido no âmbito da unidade curricular de Engenharia de Software (Iscte-IUL, 2026/2027).

![Java Version](https://img.shields.io/badge/Java-21%2B-blue)
![Build Tool](https://img.shields.io/badge/build-Maven-orange)

---

## 👥 Grupo

| Nome | Nº de estudante | Conta GitHub | Requisito (Issue) |
| :--- | :--- | :--- | :--- |
| Daniel | 129853 | [LEI-129853](https://github.com/LEI-129853) | Exportar histórico de jogadas para PDF (#1) |
| Duarte | 129851 | [LEI-129851] | Scoreboard dos jogos passados (#2) |
| Dinis | 129820 | [LEI-129820] | Tempo gasto em cada jogada (#3) |

> Atualizar os números dos issues e as contas GitHub depois de criados.

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

> Colar aqui o prompt final da estratégia, depois de melhorado pelo grupo (Diário de Bordo, sem tiros repetidos, tiros contíguos após um acerto, diagonais de um acerto são água, halo à volta dos navios afundados, etc.).

```text
(prompt final do grupo)
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
