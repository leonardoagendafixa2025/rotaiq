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

---

### ADR-010: Extração de Ofertas via Android AccessibilityService (Fase 3)
- **Status**: Aprovado e Implementado
- **Contexto**: Para que o motorista não precise digitar valores enquanto dirige, o app precisa ler ofertas instantaneamente na tela.
- **Decisão**: Implementar `RotaIqAccessibilityService` com `AccessibilityNodeExtractor`, escutando eventos de janela de apps monitorados (`com.ubercab.driver`, `com.taxis99`), com debounce de 2 segundos e deduplicação de cartões por hash.
- **Consequências**: Operação 100% mãos livres (hands-free), sem requisições de rede, em estrita conformidade com as políticas do Google Play e da LGPD.

---

### ADR-011: HUD Flutuante Translúcido em WindowManager com FLAG_NOT_FOCUSABLE (Fase 3)
- **Status**: Aprovado e Implementado
- **Contexto**: O motorista precisa ver o veredito instantâneo sem ser impedido de aceitar ou recusar a corrida no app original.
- **Decisão**: Utilizar `WindowManager` com `TYPE_APPLICATION_OVERLAY` e `FLAG_NOT_FOCUSABLE`. O card `FloatingHudView` é compacto, arrastável por toque e possui timer de auto-dismiss de 15 segundos.
- **Consequências**: Não rouba o foco do app de corrida, renderiza em menos de 100ms e permite que o motorista toque nos botões do Uber/99 normalmente.

---

### ADR-012: Síntese Vocal de Decisão (TTS pt-BR) para Copiloto "Olhos na Pista" (Fase 3)
- **Status**: Aprovado e Implementado
- **Contexto**: Olhar fixamente para o celular enquanto dirige em velocidade pode causar acidentes graves.
- **Decisão**: Implementar `VoiceAlertManager` e `TtsMessageFormatter` utilizando o `TextToSpeech` nativo do Android configurado para português brasileiro em velocidade 1.15x, vocalizando uma frase resumida em menos de 3 segundos ("Corrida Excelente! Sobra vinte e sete reais, oitenta e um por hora").
- **Consequências**: Segurança máxima ao volante e decisão instantânea sem necessidade de tirar os olhos da pista.

---

### ADR-013: Modelagem Preditiva de Deadhead e Abatimento de Volta Vazia (Fase 4)
- **Status**: Aprovado e Implementado
- **Contexto**: Corridas longas com valor bruto elevado frequentemente levam motoristas a locais ermos ou periferias sem passageiros de retorno, gerando prejuízo oculto pelo deslocamento vazio.
- **Decisão**: Criar o `DeadheadPredictorEngine`, calculando a probabilidade de retorno da zona de desembarque e abatendo o custo do retorno vazio (`expectedEmptyKm * costPerKm`) diretamente do lucro líquido da corrida (`adjustedNetProfit`).
- **Consequências**: Previne que o motorista caia em armadilhas de corridas longas que pagam bem na ida mas dão prejuízo na volta.

---

### ADR-014: Heatmap de Demanda Temporal Baseado em Janelas Horárias (Fase 4)
- **Status**: Aprovado e Implementado
- **Contexto**: O comportamento de demanda de uma mesma região varia drasticamente entre o horário comercial, pico vespertino e madrugada.
- **Decisão**: Estruturar o `GeoHeatmapEngine` com 5 janelas temporais diárias (`TimeSlot`), reclassificando os níveis de demanda de cada zona metropolitana em tempo real.
- **Consequências**: Orienta o motorista para posicionar o veículo estrategicamente nas regiões mais quentes do horário atual.

---

### ADR-015: Motor Comparativo Agregado de Eficiência Operacional por Plataforma (Fase 4)
- **Status**: Aprovado e Implementado
- **Contexto**: Motoristas frequentemente alternam entre Uber, 99 e inDrive sem clareza matemática de qual aplicativo gera mais lucro líquido por hora trabalhada.
- **Decisão**: Implementar o `PlatformComparisonEngine`, gerando relatórios consolidados de lucro/hora, faturamento, ticket médio e recomendações acionáveis.
- **Consequências**: Permite que o motorista foque no aplicativo mais rentável em cada perfil operacional.

---

### ADR-016: Sistema de Assinaturas e Planos Pro com Feature Gating (Fase 5)
- **Status**: Aprovado e Implementado
- **Contexto**: Para viabilizar comercialmente a plataforma sem depender de anúncios invasivos ao motorista, é necessário um modelo de monetização claro e de alto valor percebido.
- **Decisão**: Implementar modelo freemium estruturado com `FeatureGateManager`. O plano Free permite 15 avaliações diárias com cálculos de custos essenciais. Os planos Pro (`PRO_MONTHLY` a R$ 29,90 e `PRO_ANNUAL` a R$ 239,90 com 33% de desconto) desbloqueiam avaliações ilimitadas, HUD flutuante, síntese de voz TTS, predição de deadhead e comparativo de plataformas.
- **Consequências**: Alta conversão de motoristas frequentes, receita recorrente previsível (MRR) e valor tangível entregue ao motorista que se paga nas primeiras corridas otimizadas.

---

### ADR-017: Checkout Instantâneo via PIX Nativo EMV BR Code com CRC-16 (Fase 5)
- **Status**: Aprovado e Implementado
- **Contexto**: No Brasil, o PIX é o meio de pagamento preferido por mais de 80% dos motoristas de aplicativo devido à liquidação imediata e ausência de tarifas de cartão.
- **Decisão**: Implementar gerador de payload EMV BR Code nativo em `PixPaymentManager`, calculando CRC-16/CCITT-FALSE conforme manual do Banco Central do Brasil, gerando código "Copia e Cola" instantâneo diretamente no app sem redirecionamentos externos lentos.
- **Consequências**: Fricção mínima de compra, ativação em tempo real e maior taxa de conversão no checkout.

---

### ADR-018: Conformidade Estrita com a LGPD (Lei 13.709/2018) (Fase 5)
- **Status**: Aprovado e Implementado
- **Contexto**: O aplicativo lida com dados sensíveis de localização, faturamento e despesas dos motoristas. A conformidade com a LGPD é mandatória.
- **Decisão**: Criar o `LgpdManager` e a tela `PrivacySettingsScreen` implementando:
  1. **Art. 18, V (Portabilidade de Dados)**: Geração de pacote JSON estruturado com todos os registros do motorista.
  2. **Art. 18, VI (Direito ao Esquecimento)**: Expurgo completo de dados locais e criação de tombstone criptográfico de auditoria.
  3. **Privacy by Design**: Consentimentos explícitos e voluntários para telemetria e benchmarking.
- **Consequências**: Total proteção jurídica da plataforma e respeito à privacidade do motorista.

---

### ADR-019: Hardening de Produção R8, Obfuscation e Higienização de PII em Telemetria (Fase 5)
- **Status**: Aprovado e Implementado
- **Contexto**: A publicação em produção na Google Play Store exige regras estritas de segurança, proteção contra engenharia reversa e sigilo de dados em telemetria.
- **Decisão**: Configurar `proguard-rules.pro` com regras completas de shrinking e ofuscação de classes internas, remoção de logs de debug, e criar o `PiiSanitizer` para mascaramento automático de CPF (`***.456.789-**`), emails, telefones e placas antes do envio para qualquer fila de telemetria ou log.
- **Consequências**: Binário protegido contra engenharia reversa e conformidade com as políticas do Google Play Developer.

---

### ADR-020: Automação Hands-Free Veicular com BroadcastReceiver de Bluetooth (Fase 6)
- **Status**: Aprovado e Implementado
- **Contexto**: Motoristas frequentemente esquecem de iniciar os serviços de sobreposição ao entrar no veículo, comprometendo a captura das primeiras ofertas.
- **Decisão**: Implementar o `BluetoothCarReceiver` e o `CarModeManager` desacoplado por `CarModePreferencesDataSource`, escutando eventos do sistema `ACTION_ACL_CONNECTED` e `ACTION_ACL_DISCONNECTED`. O emparelhamento com o multimídia do carro dispara o `OverlayService` em segundo plano e notificação falada por TTS sem toque físico na tela.
- **Consequências**: Experiência hands-free transparente, maior segurança viária e zero fricção para iniciar o turno de trabalho.

---

### ADR-021: Motor de Contraproposta Preditiva para a Dinâmica inDrive (Fase 6)
- **Status**: Aprovado e Implementado
- **Contexto**: Ao contrário de Uber e 99 (tarifas pré-fixadas), o inDrive opera por modelo de leilão onde o passageiro define a oferta inicial e o motorista tem poucos segundos para aceitar ou contrapropor (+R$ 2, +R$ 4, +R$ 6...).
- **Decisão**: Criar o `InDriveParser` e o `InDriveCounterOfferEngine`, que confronta a oferta com os custos por km do veículo e a meta horária líquida, sugerindo instantaneamente o degrau exato de contraproposta com maior probabilidade de aceite que atinge a margem desejada.
- **Consequências**: Decisão ágil em menos de 3 segundos no leilão, evitando contrapropostas abusivas que perdem a corrida ou aceites subvalorizados que geram prejuízo.

---

### ADR-022: Demonstrativo Fiscal e Livro Caixa MEI/IRPF Conforme Legislação Brasileira (Fase 6)
- **Status**: Aprovado e Implementado
- **Contexto**: Mais de 70% dos motoristas de aplicativo no Brasil têm dúvidas sobre como declarar seus rendimentos e temem a malha fina da Receita Federal.
- **Decisão**: Desenvolver o `DriverTaxReportEngine` aplicando com precisão a Lei Complementar 123/2006 (art. 14) e o Regulamento do IR: presunção legal de isenção de 16% (transporte de passageiros) e 60% (entregas de mercadorias), somada à dedução de despesas operacionais comprovadas (combustíveis com nota, revisões, IPVA e seguro), com monitoramento em tempo real do teto anual do MEI (R$ 81.000,00) e exportação em CSV para DASN-SIMEI/Carnê-Leão.
- **Consequências**: Tranquilidade jurídica e contábil ao motorista, economizando custos com despachantes e comprovando isenção de IRPF.

---

### ADR-023: Teoria das Filas e Custo de Oportunidade na Rejeição Estratégica (Fase 6)
- **Status**: Aprovado e Implementado
- **Contexto**: A "ansiedade de corrida" faz motoristas aceitarem chamadas com remuneração inferior ao custo por km por receio de ficar parado.
- **Decisão**: Implementar o `StrategicRejectionEngine`, calculando matematicamente o *Break-even wait time* (Ponto de Equilíbrio de Espera): tempo máximo que o motorista pode aguardar estacionado até surgir uma oferta no patamar da sua meta horária antes de empatar com o rendimento da corrida ruim, além de computar o valor de custo operacional evitado.
- **Consequências**: Conscientização matemática do motorista, aumento da rentabilidade líquida por hora e redução de desgaste desnecessário do veículo.

---

### ADR-024: Backend-as-a-Service e Persistência em Nuvem com Supabase PostgreSQL (Fase 7)
- **Status**: Aprovado e Implementado
- **Contexto**: Para viabilizar sincronização em nuvem sem custos proibitivos de infraestrutura e com facilidade de manutenção para o ecossistema ROTA IQ, foi selecionado o Supabase (PostgreSQL 15 gerenciado).
- **Decisão**: Configurar o projeto Supabase em produção com 16 tabelas relacionais (`supabase_setup.sql`), chaves públicas (`sb_publishable_...`) e secretas (`sb_secret_...`), políticas de segurança por linha (Row Level Security - RLS) para isolamento de dados por motorista, e implementar clientes leves desacoplados tanto no backend Python quanto no aplicativo Android (`SupabaseSyncClient`).
---

### ADR-025: Redesign Visual Premium com Identidade Dark Obsidian e Electric Orange (Fase 8)
- **Status**: Aprovado e Implementado
- **Contexto**: A experiência do usuário no aplicativo necessitava de um salto estético profissional para um patamar comercial de 2026, com foco em ergonomia no trânsito, acabamento tecnológico e forte hierarquia visual.
- **Decisão**: 
  1. Adotar a paleta base em preto profundo/obsidian (`#080808`, `#0D0D0D`, `#141414`), com acentos em laranja vibrante (`#FF7A00`) para ações e progresso, e semáforo financeiro clássico (verde neon `#00E676`, amarelo `#FFD600`, vermelho `#FF334B`).
  2. Implementar o Design System ROTA IQ modular (`RotaCard`, `RotaButton`, `RotaMetric`, `RotaScore`, `RotaBottomBar`, `RotaHeader`, `RotaProgress`, `RotaChip`, `RotaStatus`, `RotaChart`) com border radius de 18-24dp, glow sutil e microinterações táteis.
  3. Redesenhar a Home com foco absoluto no Card de Meta de Hoje e no Card Principal de Corrida (R$ 32,80, grid de métricas e lucro estimado), incluindo o "Modo Motorista" de alto contraste.
  4. Redesenhar as telas de Corridas, Financeiro, Análise Estratégica, Perfil/Veículo e HUD Flutuante sob a mesma identidade visual coesa.
- **Consequências**: Clareza de leitura imediata para motoristas em movimento, percepção de valor elevada e consistência de marca 100% alinhada entre Android e Simulador Web.



