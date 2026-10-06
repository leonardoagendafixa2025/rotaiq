# Arquitetura do ROTA IQ

## 1. Visão Geral Arquitetural

O **ROTA IQ** segue os princípios de **Clean Architecture** e **Modularidade Orientada a Domínio**, com forte aderência ao princípio **Offline-First**.

```
┌─────────────────────────────────────────────────────────────┐
│                       UI / PRESENTATION                     │
│  Jetpack Compose • Material 3 • Navigation • Cockpit Theme  │
│  [Dashboard] [RideSimulator] [Vehicle] [Goals] [History]    │
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow / Actions
┌──────────────────────────────▼──────────────────────────────┐
│                         VIEWMODELS                          │
│   DashboardViewModel • SimulatorViewModel • VehicleViewModel│
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                        DOMAIN LAYER                         │
│  Motores Determinísticos Puros (Sem dependências de Framework)│
│  - RideEvaluationEngine                                     │
│  - VehicleCostEngine                                        │
│  - GoalEngine                                               │
│  - FinancialEngine                                          │
│  - OfferParser / Normalizer (Uber, 99, inDrive)             │
│  - Domain Models & Enums                                    │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                         DATA LAYER                          │
│  - RotaIqRepository (Interface & Implementação)             │
│  - Room Database (RotaIqDatabase, DAOs, Entities, Mappers)  │
│  - (Futuro) Camada Remota / Retrofit / Ktor Sync            │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Estrutura de Diretórios da Base Android

```
app/
 ├── src/
 │    ├── main/
 │    │    ├── AndroidManifest.xml
 │    │    ├── java/com/rotai/iq/
 │    │    │    ├── core/
 │    │    │    │    ├── domain/
 │    │    │    │    │    ├── model/         # Modelos de domínio puros
 │    │    │    │    │    ├── engine/        # Motores matemáticos e de decisão
 │    │    │    │    │    └── parser/        # Parsers desacoplados (Uber, 99)
 │    │    │    │    ├── data/
 │    │    │    │    │    ├── local/
 │    │    │    │    │    │    ├── db/      # RotaIqDatabase, TypeConverters
 │    │    │    │    │    │    ├── entity/  # Entidades Room
 │    │    │    │    │    │    └── dao/     # Interfaces DAO
 │    │    │    │    │    └── repository/  # Repositório e Mappers
 │    │    │    └── ui/
 │    │    │         ├── theme/            # Cores, Tipografia, Tema
 │    │    │         └── components/       # HUD, Badges, Cards
 │    │    ├── feature/
 │    │    │    ├── dashboard/             # Painel principal
 │    │    │    ├── rides/                 # Simulador e Histórico
 │    │    │    ├── vehicle/               # Custos do veículo
 │    │    │    └── goals/                 # Metas e produtividade
 │    │    ├── navigation/                 # Rotas e NavGraph Compose
 │    │    ├── MainActivity.kt
 │    │    ├── RotaIqApplication.kt
 │    │    └── RotaIqViewModelFactory.kt
 │    └── test/                            # Suíte completa de testes unitários
```

---

## 3. O Motor de Decisão (`RideEvaluationEngine`)

O motor de decisão é determinístico e multi-dimensional. Ele combina 6 dimensões de análise:

1. **Rentabilidade por Hora vs Meta Horária** (Peso 35%):
   Avalia se a taxa horária bruta e líquida atinge o target do motorista.
2. **Rentabilidade por Quilômetro vs Meta por KM** (Peso 25%):
   Compara com a meta mínima estipulada pelo motorista.
3. **Deslocamento até o Passageiro (Deadhead)** (Peso 20%):
   Penaliza severamente corridas em que a distância de embarque excede o limite tolerado ou consome grande parcela da viagem.
4. **Custo do Veículo e Margem Líquida Real** (Peso 15%):
   Subtrai o custo consolidado do veículo gerado pelo `VehicleCostEngine`. Se o custo supera a oferta, a corrida é sumariamente penalizada com classificação `EVITAR` e alerta de prejuízo.
5. **Paradas Intermediárias**:
   Aplica penalidade acumulativa para cada parada adicional devido a tempo de espera não remunerado e risco operacional.
6. **Contexto Geográfico e Zonas**:
   Aplica multiplicadores e alertas caso o destino esteja em zonas de risco ou sem retorno favorável.

---

## 4. Princípio Offline-First

O motorista frequentemente opera em túneis, rodovias ou áreas com oscilação de sinal 4G/5G.
- Todas as avaliações de corridas e cálculos ocorrem localmente no dispositivo em microssegundos.
- Todos os dados (veículos, metas, histórico) são persistidos no banco local SQLite/Room.
- Na Fase 2 e Fase 5, a sincronização com o backend ocorrerá de forma assíncrona em segundo plano via WorkManager.
