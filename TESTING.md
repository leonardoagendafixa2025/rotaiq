# Estratégia de Testes Automatizados

## 1. Visão Geral da Suíte de Testes

A integridade matemática dos cálculos financeiros e das decisões operacionais é o coração do **ROTA IQ**. A suíte de testes unitários conta com **43 cenários de teste automatizados** (13 suítes de teste) cobrindo todos os motores de decisão, parsers, acessibilidade, sintetizador de voz e sincronização.

---

## 2. Cobertura das Classes de Teste

### 2.1 `RideEvaluationEngineTest` (9 Cenários)
- Valida nota >= 80, classificação `EXCELENTE`, margem de lucro saudável e lista de justificativas positivas.
- Valida detecção de corrida deficitária (abaixo do custo do veículo), nota baixa (< 45) e classificação `EVITAR`.
- Valida corrida curta com alta taxa horária (> R$ 90/h).
- Valida penalização severa quando a distância até o passageiro consome a rentabilidade (deadhead).
- Valida perda de pontos progressiva proporcional ao número de paradas intermediárias.
- Valida detecção de armadilha de trânsito em corridas com valor facial alto mas duração desproporcional.
- Valida identificação de zona de risco ou retorno desfavorável com emissão de alerta.
- Valida sensibilidade do motor ao custo real de diferentes veículos (ex: SUV a gasolina vs Sedan com GNV).

### 2.2 `VehicleCostEngineTest` (4 Cenários)
- Valida decomposição exata de custos variáveis (combustível, manutenção) e custos fixos proporcionais.
- Valida benefício de custo por km reduzido para veículos econômicos com GNV.
- Valida veículo de alto consumo e altos encargos.
- Valida projeções de custo fixo por dia, mês e ano.

### 2.3 `FinancialEngineTest` (2 Cenários)
- Valida lucro líquido, margem operacional (%), R$/km bruto/líquido, R$/h e R$/minuto.
- Valida relatórios e margens negativas em corridas com prejuízo.

### 2.4 `AdvancedFinancialEngineTest` (2 Cenários - Fase 2)
- Valida agregação completa de receitas brutas, despesas com combustível, manutenção, custos fixos, apuração de lucro líquido e margens operacionais percentuais.
- Valida o cálculo exato do ponto de equilíbrio (faturamento mínimo necessário para cobrir os custos fixos diários).

### 2.5 `FuelEngineTest` (3 Cenários - Fase 2)
- Valida o método dos 2 tanques cheios, calculando o consumo real em km/L e custo real por km na bomba com precisão centesimal.
- Valida descarte correto de abastecimentos parciais no cálculo de consumo.
- Valida o cálculo da média ponderada de consumo entre múltiplos tanques.

### 2.6 `MaintenanceSchedulerEngineTest` (3 Cenários - Fase 2)
- Valida status `OK` para manutenções com odômetro restante seguro.
- Valida status `UPCOMING` e emissão de alerta para serviços com menos de 1.000 km restantes.
- Valida status `OVERDUE` e alerta de quilometragem excedida.

### 2.7 `GoalEngineTest` (3 Cenários)
- Valida detecção de ritmo saudável e projeção de faturamento no turno.
- Valida alerta de atraso e cálculo da taxa horária necessária para alcançar a meta.
- Valida comemoração de meta diária alcançada.

### 2.8 `MultiPeriodGoalEngineTest` (3 Cenários - Fase 2)
- Valida cálculo unificado das metas diária, semanal e mensal, percentuais e orientações de ritmo.
- Valida transição de foco para a meta semanal quando a diária já foi atingida.
- Valida projeção de faturamento/hora necessário nas horas restantes do turno.

### 2.9 `OfferParserTest` (6 Cenários)
- Valida extração de quantias em reais com formatação brasileira (`OfferNormalizer`).
- Valida extração de distâncias de viagem e de busca.
- Valida extração de durações em minutos.
- Valida identificação de paradas intermediárias.
- Valida parsing completo de cartões de oferta Uber (`UberParser`).
- Valida parsing completo de cartões de oferta 99 (`NinetyNineParser`).

### 2.10 `SyncManagerTest` (2 Cenários - Fase 2)
- Valida compilação de payload para sincronização com registros não sincronizados.
- Valida processamento de confirmação do servidor (Ack) e tratamento de falhas.

### 2.11 `TtsMessageFormatterTest` (3 Cenários - Fase 3)
- **`formatSpeechMessage_excellentRide_returnsEncouragingSpeech`**: Valida formulação concisa de corrida excelente, verbalizando lucro líquido limpo arredondado e taxa horária.
- **`formatSpeechMessage_avoidRideNegativeProfit_warnsAboutLoss`**: Valida verbalização enfática de alerta e cálculo falado do prejuízo estimado.
- **`formatSpeechMessage_acceptableRide_includesAlertWarning`**: Valida inclusão do aviso sonoro de alerta sobre paradas ou margem intermediária.

### 2.12 `AccessibilityNodeExtractorTest` (3 Cenários - Fase 3)
- **`buildCombinedText_emptyList_returnsEmptyString`**: Valida segurança e resiliência com lista vazia.
- **`buildCombinedText_multipleLines_joinsCorrectlyWithNewlines`**: Valida reconstrução limpa e sequencial do texto do cartão de oferta a partir de múltiplos nós de tela.
- **`extractAllTexts_nullNode_returnsEmptyList`**: Valida tratamento gracioso de nós de acessibilidade nulos.

### 2.13 `OfferRealTimeParsingPipelineTest` (2 Cenários - Fase 3)
- **`livePipeline_uberCard_extractsEvaluatesAndGeneratesSpokenAlert`**: Teste de integração de ponta a ponta: nós brutos da tela do Uber -> detecção -> parsing -> avaliação matemática -> geração de frase de voz TTS.
- **`livePipeline_ninetyNineTrapCard_identifiesDeficitAndGeneratesAvoidSpeech`**: Teste de integração de ponta a ponta para armadilha de cartão da 99 com 2 paradas, diagnosticando classificação `EVITAR` e gerando alerta sonoro correspondente.

---

## 3. Como Executar os Testes

Via terminal / PowerShell:
```powershell
.\gradlew.bat testDebugUnitTest
```

Para inspecionar o relatório HTML gerado pelo Gradle:
`app/build/reports/tests/testDebugUnitTest/index.html`
