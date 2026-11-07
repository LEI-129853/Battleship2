# Comentários à análise Qodana: BattleShip2

- O Qodana detetou 24 problemas no total, todos de severidade High, o que mostra que os problemas encontrados merecem atenção e não são apenas questões de estilo.
- A categoria dominante é Security (15 problemas, cerca de 63%), todos de "Vulnerable declared dependency" no `pom.xml`: dependências diretas (`jackson-databind`, `commons-lang3`) e transitivas trazidas pelo Spring Boot (Spring, Tomcat, Logback, log4j). Resolve-se sobretudo subindo a versão do `spring-boot-starter-parent`.
- Seguem-se os Probable bugs, concentrados em `Ship.java` (condição `this != null` sempre true, atribuições redundantes e indentação suspeita após `if`, que pode esconder erros de lógica) e um unused assignment em `Game.java`.
- Há ainda dois problemas de Performance em `Game.java` (`set.removeAll(list)` pode ser lento) e um de Threading em `Tasks.java` (`Thread.sleep()` em ciclo, ou seja, busy-wait).
- Os resultados coincidem com a inspeção manual do IntelliJ (Code → Inspect Code), o que dá confiança nos Issues criados. O Qodana acrescenta a vantagem de produzir um relatório navegável por categoria e severidade, e de poder ser integrado em CI.
- O ficheiro `qodana.yaml` usa o perfil `qodana.starter` e não define `failureConditions`, por isso a análise não falha por si só. Na Tarefa 2 vamos acrescentar condições de falha (quality gate).