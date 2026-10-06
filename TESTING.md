# Estratégia de Testes Automatizados

## 1. Visão Geral da Suíte de Testes

A integridade matemática dos cálculos financeiros e das notas de decisão de corrida é o coração do **ROTA IQ**. Por isso, a suíte de testes unitários da Fase 1 valida exaustivamente os motores de decisão com **24 cenários de teste automatizados**.

---

## 2. Cobertura das Classes de Teste

### 2.1 `RideEvaluationEngineTest` (9 Cenários)
- **`evaluate_idealRide_returnsExcellentScore`**:
  Testa uma corrida com excelente margem (R$ 32,80 por 9,4 km em 26 min, passageiro a 1 km). Valida nota >= 80, badge `EXCELENTE`, lucro líquido positivo e lista de motivos.
- **`evaluate_lowRateLongRide_returnsAvoidOrBad`**:
  Testa corrida que paga R$ 20,00 por 25 km em 50 min (R$ 0,80/km). Valida nota < 45, classificação `RUIM` ou `EVITAR` e alerta de rentabilidade abaixo do custo do veículo.
- **`evaluate_shortProfitableRide_returnsGoodOrExcellent`**:
  Testa corrida curta altamente rentável (R$ 16,00 por 2,5 km em 8 min, embarque a 500m). Valida taxa horária superior a R$ 90/h e nota alta.
- **`evaluate_farPickupDeadhead_penalizesScoreWithAlert`**:
  Testa viagem curta de 3 km com passageiro distante a 6 km. Valida penalidade de deslocamento (deadhead) e emissão de alerta.
- **`evaluate_multipleStops_appliesStopPenalties`**:
  Compara duas corridas idênticas em valor e km, sendo uma com 3 paradas intermediárias. Valida redução de score e alerta de paradas.
- **`evaluate_highGrossValueLowHourly_detectsTrafficTrap`**:
  Testa uma corrida com valor bruto alto (R$ 70,00), mas tempo longo em engarrafamento (150 min). Valida detecção de baixa taxa horária e penalização.
- **`evaluate_avoidZoneDestination_penalizesAndAlerts`**:
  Testa destino em região classificada como `isAvoidZone = true`. Valida redução severa da pontuação e alerta imediato.
- **`evaluate_highCostVehicleVsLowCostVehicle_producesDifferentProfitAndScore`**:
  Compara a avaliação da mesma corrida sob dois veículos diferentes: um SUV a gasolina (alto custo) vs um carro com GNV (baixo custo). Valida lucro líquido e score divergentes.

### 2.2 `VehicleCostEngineTest` (4 Cenários)
- **`calculateTripCost_standardVehicle_computesCorrectBreakdown`**: Valida a decomposição exata entre custo de combustível, manutenção preventiva e custos fixos proporcionais.
- **`calculateTripCost_lowCostGnvVehicle_hasMuchLowerCostPerKm`**: Valida veículo com GNV apresentando custo significativamente menor por km.
- **`calculateTripCost_highCostGasGuzzler_hasHighCostPerKm`**: Valida veículo de alto consumo e altos custos fixos.
- **`calculateProjections_computesAccurateHourlyAndAnnualProjections`**: Valida projeções de custo por hora, custo fixo diário, custo mensal e custo anual total.

### 2.3 `FinancialEngineTest` (2 Cenários)
- **`calculateMetrics_normalRide_computesAccurateFinancials`**: Valida cálculo de lucro líquido, margem percentual, R$/km bruto/líquido, R$/h bruto/líquido e R$/minuto.
- **`calculateMetrics_negativeProfitRide_reportsNegativeMargin`**: Valida comportamento com corridas deficitárias.

### 2.4 `GoalEngineTest` (3 Cenários)
- **`evaluateGoalProgress_onTrack_producesEncouragingAdvice`**: Valida detecção de ritmo saudável e cálculo de projeção de fechamento de turno.
- **`evaluateGoalProgress_behindPace_warnsDriver`**: Valida emissão de alerta de atraso e cálculo exato de R$/h necessário para recuperar a meta.
- **`evaluateGoalProgress_goalReached_celebratesSuccess`**: Valida status de meta atingida e mensagens comemorativas.

### 2.5 `OfferParserTest` (6 Cenários)
- **`offerNormalizer_parsesBrlCurrency`**: Valida parsing de valores monetários com vírgula e separador de milhar (`R$ 32,80`, `R$ 1.250,50`, `$ 15,00`).
- **`offerNormalizer_parsesKmDistances`**: Valida extração de quilometragens de embarque e viagem.
- **`offerNormalizer_parsesDurations`**: Valida extração de tempos em minutos.
- **`offerNormalizer_parsesStopsCount`**: Valida detecção de quantidade de paradas.
- **`uberParser_parsesTypicalCard`**: Valida parsing completo de cartão de oferta da Uber.
- **`ninetyNineParser_parsesTypicalCard`**: Valida parsing completo de cartão de oferta da 99.
- **`platformDetector_routesToCorrectParser`**: Valida roteamento dinâmico baseado em texto e identificadores.

---

## 3. Como Executar os Testes

Via terminal:
```powershell
.\gradlew.bat testDebugUnitTest
```

Para visualizar o relatório HTML gerado pelo Gradle:
`app/build/reports/tests/testDebugUnitTest/index.html`
