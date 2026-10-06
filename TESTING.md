# Estratégia de Testes Automatizados

## 1. Visão Geral da Suíte de Testes

A integridade matemática dos cálculos financeiros e das decisões operacionais é o coração do **ROTA IQ**. A suíte de testes unitários conta com **37 cenários de teste automatizados** cobrindo todos os motores de decisão, parsers e camadas de sincronização.

---

## 2. Cobertura das Classes de Teste

### 2.1 `RideEvaluationEngineTest` (9 Cenários)
- **`evaluate_idealRide_returnsExcellentScore`**: Valida nota >= 80, classificação `EXCELENTE`, margem de lucro saudável e lista de justificativas positivas.
- **`evaluate_lowRateLongRide_returnsAvoidOrBad`**: Valida detecção de corrida deficitária (abaixo do custo do veículo), nota baixa (< 45) e classificação `EVITAR`.
- **`evaluate_shortProfitableRide_returnsGoodOrExcellent`**: Valida corrida curta com alta taxa horária (> R$ 90/h).
- **`evaluate_farPickupDeadhead_penalizesScoreWithAlert`**: Valida penalização severa quando a distância até o passageiro consome a rentabilidade.
- **`evaluate_multipleStops_appliesStopPenalties`**: Valida perda de pontos progressiva proporcional ao número de paradas intermediárias.
- **`evaluate_highGrossValueLowHourly_detectsTrafficTrap`**: Valida detecção de armadilha de trânsito em corridas com valor facial alto mas duração desproporcional.
- **`evaluate_avoidZoneDestination_penalizesAndAlerts`**: Valida identificação de zona de risco ou retorno desfavorável com emissão de alerta.
- **`evaluate_highCostVehicleVsLowCostVehicle_producesDifferentProfitAndScore`**: Valida sensibilidade do motor ao custo real de diferentes veículos (ex: SUV a gasolina vs Sedan com GNV).

### 2.2 `VehicleCostEngineTest` (4 Cenários)
- **`calculateTripCost_standardVehicle_computesCorrectBreakdown`**: Valida decomposição exata de custos variáveis (combustível, manutenção) e custos fixos proporcionais.
- **`calculateTripCost_lowCostGnvVehicle_hasMuchLowerCostPerKm`**: Valida benefício de custo por km reduzido para veículos econômicos.
- **`calculateTripCost_highCostGasGuzzler_hasHighCostPerKm`**: Valida veículo de alto consumo e altos encargos.
- **`calculateProjections_computesAccurateHourlyAndAnnualProjections`**: Valida projeções de custo fixo por dia, mês e ano.

### 2.3 `FinancialEngineTest` (2 Cenários)
- **`calculateMetrics_normalRide_computesAccurateFinancials`**: Valida lucro líquido, margem operacional (%), R$/km bruto/líquido, R$/h e R$/minuto.
- **`calculateMetrics_negativeProfitRide_reportsNegativeMargin`**: Valida relatórios e margens negativas em corridas com prejuízo.

### 2.4 `AdvancedFinancialEngineTest` (2 Cenários - Fase 2)
- **`consolidatePeriodReport_computesAccurateConsolidation`**: Valida agregação completa de receitas brutas, despesas com combustível, manutenção, custos fixos, apuração de lucro líquido e margens operacionais percentuais.
- **`calculateBreakEvenDailyRevenue_returnsCorrectBreakEven`**: Valida o cálculo exato do ponto de equilíbrio (faturamento mínimo necessário para cobrir os custos fixos diários).

### 2.5 `FuelEngineTest` (3 Cenários - Fase 2)
- **`calculateConsumptionBetweenFills_validConsecutiveTanks_calculatesExactKmPerLiter`**: Valida o método dos 2 tanques cheios, calculando o consumo real em km/L e custo real por km na bomba com precisão centesimal.
- **`calculateConsumptionBetweenFills_partialTank_returnsNull`**: Valida descarte correto de abastecimentos parciais no cálculo de consumo.
- **`calculateAverageConsumption_multipleFills_returnsWeightedAverage`**: Valida o cálculo da média ponderada de consumo entre múltiplos tanques.

### 2.6 `MaintenanceSchedulerEngineTest` (3 Cenários - Fase 2)
- **`evaluateServiceStatus_serviceUpToDate_returnsOk`**: Valida status `OK` para manutenções com odômetro restante seguro.
- **`evaluateServiceStatus_serviceUpcoming_returnsUpcomingWarning`**: Valida status `UPCOMING` e emissão de alerta para serviços com menos de 1.000 km restantes.
- **`evaluateServiceStatus_serviceOverdue_returnsOverdueAlert`**: Valida status `OVERDUE` e alerta de quilometragem excedida.

### 2.7 `GoalEngineTest` (3 Cenários)
- **`evaluateGoalProgress_onTrack_producesEncouragingAdvice`**: Valida detecção de ritmo saudável e projeção de faturamento no turno.
- **`evaluateGoalProgress_behindPace_warnsDriver`**: Valida alerta de atraso e cálculo da taxa horária necessária para alcançar a meta.
- **`evaluateGoalProgress_goalReached_celebratesSuccess`**: Valida comemoração de meta diária alcançada.

### 2.8 `MultiPeriodGoalEngineTest` (3 Cenários - Fase 2)
- **`calculateProgress_normalPace_returnsAccurateMetrics`**: Valida cálculo unificado das metas diária, semanal e mensal, percentuais e orientações de ritmo.
- **`calculateProgress_dailyCompleted_celebratesAndFocusesOnWeekly`**: Valida transição de foco para a meta semanal quando a diária já foi atingida.
- **`calculateProgress_behindPace_indicatesRequiredPace`**: Valida projeção de faturamento/hora necessário nas horas restantes do turno.

### 2.9 `OfferParserTest` (6 Cenários)
- **`offerNormalizer_parsesBrlCurrency`**: Valida extração de quantias em reais com formatação brasileira.
- **`offerNormalizer_parsesKmDistances`**: Valida extração de distâncias de viagem e de busca.
- **`offerNormalizer_parsesDurations`**: Valida extração de durações em minutos.
- **`offerNormalizer_parsesStopsCount`**: Valida identificação de paradas intermediárias.
- **`uberParser_parsesTypicalCard`**: Valida parsing completo de cartões de oferta Uber.
- **`ninetyNineParser_parsesTypicalCard`**: Valida parsing completo de cartões de oferta 99.

### 2.10 `SyncManagerTest` (2 Cenários - Fase 2)
- **`preparePushPayload_aggregatesUnsyncedDataCorrectly`**: Valida compilação de payload para sincronização com registros não sincronizados.
- **`processSyncResponse_handlesSuccessAndFailure`**: Valida processamento de confirmação do servidor (Ack) e tratamento de falhas.

---

## 3. Como Executar os Testes

Via terminal / PowerShell:
```powershell
.\gradlew.bat testDebugUnitTest
```

Para inspecionar o relatório HTML gerado pelo Gradle:
`app/build/reports/tests/testDebugUnitTest/index.html`
