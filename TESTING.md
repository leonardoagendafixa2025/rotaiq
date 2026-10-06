# Estratégia de Testes Automatizados

## 1. Visão Geral da Suíte de Testes

A integridade matemática dos cálculos financeiros, das decisões operacionais, da segurança e do faturamento comercial é o coração do **ROTA IQ**. A suíte de testes unitários conta com **73 cenários de teste automatizados** (22 suítes de teste) com 100% de taxa de aprovação (`BUILD SUCCESSFUL`).

---

## 2. Cobertura das Classes de Teste

### 2.1 `FeatureGateManagerTest` (4 Cenários - Fase 5)
- Valida controle de limite de avaliações diárias gratuitas (permite até 15 avaliações/dia, bloqueia a 16ª).
- Valida liberação de avaliações ilimitadas e de todos os recursos para assinantes Pro ativos.
- Valida bloqueio explícito de recursos Pro no plano gratuito (HUD flutuante, TTS, Deadhead, Comparativo).
- Valida downgrade automático para regras gratuitas quando a assinatura Pro expira.

### 2.2 `PixPaymentManagerTest` (3 Cenários - Fase 5)
- Valida geração de payload EMV BR Code oficial BACEN (Payload Indicator `000201`, GUI `br.gov.bcb.pix`, chave, valor, CRC16).
- Valida integridade e cálculo de algoritmo CRC-16/CCITT-FALSE (polinômio 0x1021, valor inicial 0xFFFF).
- Valida detecção de adulteração em payload PIX corrompido.

### 2.3 `BillingManagerTest` (4 Cenários - Fase 5)
- Valida processamento de compra e concessão de entitlements para `PRO_MONTHLY` via Google Play.
- Valida cálculo de período anual (365 dias) para `PRO_ANNUAL`.
- Valida ativação de assinatura via confirmação de pedido PIX.
- Valida cancelamento e manutenção do acesso até o término da vigência.

### 2.4 `PiiSanitizerTest` (5 Cenários - Fase 5)
- Valida mascaramento de CPF com e sem pontuação (`***.456.789-**`).
- Valida mascaramento de emails preservando provedor (`m***a@gmail.com`).
- Valida mascaramento de placas de veículo padrão antigo e Mercosul (`ABC-****`).
- Valida sanitização de dados pessoais em sentenças livres para logs.
- Valida ofuscação de coordenadas GPS reduzindo precisão para ~1.1km.

### 2.5 `LgpdManagerTest` (2 Cenários - Fase 5)
- Valida geração de pacote de dados estruturado em JSON para portabilidade (Art. 18, V da LGPD).
- Valida registro de solicitação de exclusão definitiva com tombstone criptográfico (Art. 18, VI).

### 2.6 `TelemetryManagerTest` (3 Cenários - Fase 5)
- Valida gravação de eventos e sanitização compulsória de PII nas propriedades.
- Valida política de rotação da fila local (descarte do mais antigo quando atinge capacidade máxima).
- Valida expurgo completo da fila.

### 2.7 `DeadheadPredictorEngineTest` (3 Cenários - Fase 4)
- Valida detecção de armadilha de deadhead e cálculo de custo de retorno vazio.
- Valida zona central de alta liquidez com baixo deadhead.
- Valida retorno sem passageiro onde o custo de volta transforma a viagem em prejuízo.

### 2.8 `PlatformComparisonEngineTest` (2 Cenários - Fase 4)
- Valida consolidação de métricas entre Uber e 99 (R$/h, R$/km, margem).
- Valida geração de recomendação tática quando uma plataforma supera a outra em rentabilidade líquida.

### 2.9 `GeoHeatmapEngineTest` (2 Cenários - Fase 4)
- Valida reclassificação de demanda em 5 janelas horárias.
- Valida retorno de zonas metropolitanas e probabilidades de retorno de passageiro.

### 2.10 `RideEvaluationEngineTest` (9 Cenários)
- Valida nota >= 80, classificação `EXCELENTE`, margem de lucro saudável.
- Valida detecção de corrida deficitária, nota baixa (< 45) e classificação `EVITAR`.
- Valida corrida curta com alta taxa horária (> R$ 90/h).
- Valida penalização progressiva proporcional ao número de paradas intermediárias.
- Valida sensibilidade do motor ao custo real de diferentes veículos (SUV a gasolina vs Sedan GNV).

### 2.11 `VehicleCostEngineTest` (4 Cenários)
- Valida decomposição exata de custos variáveis (combustível, manutenção) e fixos proporcionais.
- Valida custo por km reduzido para veículos com GNV.
- Valida projeções de custo fixo por dia, mês e ano.

### 2.12 `FinancialEngineTest` (2 Cenários)
- Valida lucro líquido, margem operacional (%), R$/km e R$/h.
- Valida relatórios e margens negativas em corridas deficitárias.

### 2.13 `AdvancedFinancialEngineTest` (2 Cenários - Fase 2)
- Valida agregação completa de receitas brutas, despesas e lucro líquido no DRE consolidado.
- Valida cálculo do ponto de equilíbrio operacional diário.

### 2.14 `FuelEngineTest` (3 Cenários - Fase 2)
- Valida método dos 2 tanques cheios na bomba com precisão centesimal.
- Valida descarte correto de abastecimentos parciais.

### 2.15 `MaintenanceSchedulerEngineTest` (3 Cenários - Fase 2)
- Valida status `OK`, `UPCOMING` e `OVERDUE` com alertas de odômetro.

### 2.16 `GoalEngineTest` (3 Cenários)
- Valida ritmo saudável, alerta de atraso e comemoração de meta alcançada.

### 2.17 `MultiPeriodGoalEngineTest` (3 Cenários - Fase 2)
- Valida metas diária, semanal e mensal consolidadas.

### 2.18 `OfferParserTest` (6 Cenários)
- Valida normalização monetária brasileira, distâncias, durações e paradas.

### 2.19 `OfferRealTimeParsingPipelineTest` (3 Cenários)
- Valida detecção automática do pacote e pipeline de ponta a ponta.

### 2.20 `AccessibilityNodeExtractorTest` (2 Cenários - Fase 3)
- Valida extração hierárquica de nós de acessibilidade.

### 2.21 `TtsMessageFormatterTest` (4 Cenários - Fase 3)
- Valida frases faladas em português brasileiro concisas (< 3 segundos).

### 2.22 `SyncManagerTest` (3 Cenários - Fase 2)
- Valida envio de payload de sincronização e reconciliação com PostgreSQL.

---

## 3. Como Executar a Suíte de Testes

```powershell
$env:JAVA_HOME = 'C:\Users\User\AppData\Local\Programs\Microsoft\jdk-17.0.10.7-hotspot'
$env:ANDROID_HOME = 'C:\Users\User\AppData\Local\Android\Sdk'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
& 'C:\Users\User\AppData\Local\Programs\gradle-8.7\bin\gradle.bat' testDebugUnitTest
```

Resultado verificado:
```
BUILD SUCCESSFUL in 1m 20s
22 test suites | 73 unit tests | 0 failures | 0 errors | 100% passing
```
