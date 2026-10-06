# Registro de Decisões Arquiteturais (ADRs)

Este documento registra as principais decisões técnicas e de design tomadas no **ROTA IQ**.

---

### ADR-001: Desenvolvimento Android Nativo em Kotlin com Jetpack Compose
- **Status**: Aprovado e Implementado
- **Contexto**: Plataformas híbridas (React Native, Flutter) introduzem pontes (bridges) pesadas, maior consumo de memória e fricção ao lidar com serviços de sistema em segundo plano (`AccessibilityService`, `WindowManager` Overlay HUD).
- **Decisão**: Utilizar Kotlin moderno com Jetpack Compose e Material 3.
- **Consequências**: Desempenho máximo, consumo mínimo de bateria, acesso direto às APIs do sistema operacional e animações fluidas.

---

### ADR-002: Motores de Decisão Determinísticos e Isolados no Domínio
- **Status**: Aprovado e Implementado
- **Contexto**: A avaliação de uma corrida precisa ser instantânea (menos de 100ms) enquanto o motorista está dirigindo. Não se deve usar LLMs ou IA generativa para cálculos aritméticos e ponderações que são matematicamente determinísticas.
- **Decisão**: Criar `RideEvaluationEngine`, `VehicleCostEngine`, `GoalEngine` e `FinancialEngine` como módulos puros em Kotlin, sem qualquer dependência do framework Android ou rede.
- **Consequências**: Velocidade sub-milissegundo, testabilidade unitária de 100%, previsibilidade total e capacidade de funcionar completamente offline.

---

### ADR-003: Persistência Local com Room e Reatividade com Flow
- **Status**: Aprovado e Implementado
- **Contexto**: O motorista frequentemente enfrenta perda de conectividade celular durante corridas.
- **Decisão**: Toda leitura e escrita passa primeiro pelo banco local SQLite via Android Room e emite atualizações via Kotlin `Flow`.
- **Consequências**: Interface sempre responsiva e dados protegidos contra quedas de sinal.

---

### ADR-004: Identidade Visual Própria (Cockpit Dark & High Contrast)
- **Status**: Aprovado e Implementado
- **Contexto**: Motoristas passam até 12 horas diárias no trânsito, boa parte à noite ou sob luz solar intensa. Copiar cores de concorrentes (Uber/99) geraria confusão visual.
- **Decisão**: Criar o tema Cockpit Dark com fundo `#0A0E17`, tipografia grande e legível, e cores semânticas vibrantes (`Cyber Emerald`, `Warning Orange`, `Alert Crimson`, `Electric Cyan`).
- **Consequências**: Redução drástica de cansaço ocular, economia de bateria em telas OLED e resposta visual instantânea.

---

### ADR-005: Desacoplamento da Camada de Parsers de Ofertas
- **Status**: Aprovado e Implementado
- **Contexto**: Plataformas como Uber e 99 mudam seus layouts e textos periodicamente.
- **Decisão**: Criar a interface `OfferParser` com implementações específicas (`UberParser`, `NinetyNineParser`) e o `OfferNormalizer`. O motor de decisão não conhece strings específicas de plataformas.
- **Consequências**: Facilidade de manutenção e extensão para novas plataformas (ex: inDrive, Lalamove, Rappi) sem alterar o núcleo.

---

### ADR-006: Independência do Android Studio e Execução CLI no Antigravity
- **Status**: Aprovado e Implementado
- **Contexto**: O projeto deve ser compilável, testável e gerenciável 100% via terminal pelo ambiente Antigravity.
- **Decisão**: Configurar Gradle 8.7, Version Catalog e Android SDK Command-line Tools via linha de comando.
- **Consequências**: Automação contínua, agilidade e total independência de interfaces gráficas pesadas.

---

### ADR-007: Método de Dois Tanques Cheios para Cálculo de Consumo Real (Fase 2)
- **Status**: Aprovado e Implementado
- **Contexto**: O computador de bordo dos veículos frequentemente apresenta erro de 10% a 15%, e cálculos baseados em abastecimentos parciais são imprecisos.
- **Decisão**: O `FuelEngine` exige a marcação de tanque cheio (`isFullTank = true`) em abastecimentos consecutivos para calcular o consumo exato da bomba: `(odômetro_2 - odômetro_1) / litros_2`.
- **Consequências**: Precisão científica no custo real de combustível por km (`realCostPerKm`), permitindo calibrar o `VehicleCostEngine` com dados reais do motorista.

---

### ADR-008: Alertas Preventivos de Manutenção Baseados em Odômetro (Fase 2)
- **Status**: Aprovado e Implementado
- **Contexto**: O desgaste prematuro do veículo é uma das maiores causas de prejuízo oculto para motoristas de aplicativo.
- **Decisão**: Implementar `MaintenanceSchedulerEngine` com três faixas de urgência (`OK`, `UPCOMING`, `OVERDUE`) monitorando o odômetro e os intervalos recomendados de serviços (óleo, pneus, freios, suspensão, revisão).
- **Consequências**: Previne quebras que paralisam a operação do motorista e garante reserva financeira para manutenções programadas.

---

### ADR-009: Arquitetura de Sincronização Outbox / Push-Ack com PostgreSQL (Fase 2)
- **Status**: Aprovado e Implementado
- **Contexto**: Manter os dados sincronizados com a nuvem sem comprometer a operação em áreas de sombra ou sem rede.
- **Decisão**: Armazenar flag `syncedWithServer = false` em cada registro local e sincronizar em lotes via `SyncManager` enviando `SyncPushPayload` e recebendo confirmação de IDs do servidor PostgreSQL.
- **Consequências**: Tolerância total a falhas de rede, sem risco de perda de lançamentos financeiros e sem bloqueio da interface do usuário.
