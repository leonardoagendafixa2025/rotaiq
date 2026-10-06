# Segurança, Privacidade e Diretrizes de Conformidade

## 1. Princípios de Segurança e Privacidade

O **ROTA IQ** é projetado sob os princípios de **Privacy by Design** e estrita conformidade com as políticas do Google Play e a **LGPD (Lei Geral de Proteção de Dados - Lei 13.709/2018)**.

### Regras Fundamentais:
1. **Não capturar senhas ou credenciais**: O sistema nunca lê senhas, tokens de autenticação ou dados bancários de qualquer aplicativo.
2. **Não violar APIs privadas**: O ROTA IQ não realiza engenharia reversa de APIs fechadas da Uber ou 99.
3. **Não alterar aplicativos de terceiros**: Nenhum APK ou memória de processos de terceiros é alterada ou injetada.
4. **Processamento 100% Local (On-Device)**: Toda a análise inicial de custos, cálculo de notas, estimativa de deadhead e síntese de voz ocorrem localmente no dispositivo em menos de 2 milissegundos, garantindo que o motorista decida sem depender de rede.

---

## 2. Conformidade Estrita com a LGPD (Lei 13.709/2018 - Fase 5)

Implementada a arquitetura completa em `LgpdManager` e a tela `PrivacySettingsScreen`:

### 2.1 Direito de Acesso e Portabilidade (Art. 18, V)
- O motorista pode gerar e exportar um pacote estruturado em formato **JSON** (`exportDriverData`) com todos os seus dados operacionais:
  * Perfil cadastral (com PII mascarada);
  * Contagem de veículos registrados;
  * Histórico de avaliações de corridas;
  * Registros de abastecimento e despesas;
  * Status de consentimentos ativos.

### 2.2 Direito ao Esquecimento e Eliminação de Dados (Art. 18, VI)
- Expurgo atômico de dados pessoais através da funcionalidade "Excluir Minha Conta e Limpar Dados":
  * Limpeza completa do banco local Room e de todas as SharedPreferences;
  * Geração de tombstone criptográfico de auditoria com hash SHA-256 (`DEL-...`);
  * Revogação imediata de tokens e restauração do aplicativo para estado de fábrica.

### 2.3 Consentimento Explícito e Privacy by Design
- Gestão granular de consentimentos através de `LgpdConsent`:
  * Termos de Uso e Política de Privacidade obrigatórios;
  * Opt-in voluntário para telemetria técnica de diagnóstico;
  * Opt-in voluntário para benchmarking anônimo de regiões metropolitanas.

---

## 3. Sanitização Compulsória de Dados Pessoais (`PiiSanitizer`)

Nenhum dado pessoal identificável (PII) é enviado em texto puro para logs ou telemetria:
- **CPF**: Mascarado automaticamente no formato `***.456.789-**`.
- **E-mail**: Mascarado preservando apenas inicial e provedor (`m***a@gmail.com`).
- **Telefone**: Mascarado preservando DDD e sufixo (`(11) 9****-**21`).
- **Placas de Veículo**: Mascaradas para `ABC-****` (padrão antigo e Mercosul).
- **Coordenadas Geográficas**: Truncamento proposital da precisão para ~1.1km para proteger residências e pontos sensíveis.

---

## 4. Armazenamento Seguro e Criptografia (`SecureStorage`)

- Implementado `AndroidSecureStorage` com algoritmo **AES-256 GCM**:
  * Utiliza chaves simétricas protegidas de 256 bits geradas de forma aleatória e segura;
  * Vetores de inicialização (IV) de 12 bytes gerados por `SecureRandom` para cada gravação;
  * Armazenamento seguro de tokens de compra do Google Play, tokens de sessão e credenciais sensíveis.
- Toda a comunicação de rede remota com a API utiliza **TLS 1.3** com verificação estrita de certificados HTTPS.

---

## 5. Hardening de Produção R8 e Ofuscação (`proguard-rules.pro`)

Regras completas de segurança e otimização configuradas para compilação de release:
- **Repackaging**: Classes internas consolidadas no pacote ofuscado `com.rotai.iq.obf`.
- **Remoção de Logs**: Chamadas a `Log.d` e `Log.v` removidas em tempo de compilação pelo bytecode optimizer.
- **Proteção de Domínio**: Algoritmos de pontuação, cálculo de deadhead e parsers ofuscados para impedir extração por concorrentes.
- **Whitelist Segura**: Apenas entidades Room, modelos de dados e DAOs necessários para reflexão/serialização preservados.

---

## 6. Conformidade com o Android AccessibilityService

O `RotaIqAccessibilityService` segue as políticas da Google Play Store:
- **Finalidade Declarada**: Auxílio de tomada de decisão e segurança ao volante para motoristas profissionais.
- **Escopo Restrito**: O serviço apenas monitora `com.ubercab.driver` e `com.taxis99`.
- **Filtro de Conteúdo**: O extrator de texto (`AccessibilityNodeExtractor`) apenas coleta nós públicos do cartão de oferta. Conteúdos de chats particulares, passageiros ou pagamentos são ignorados.
- **Transparência**: Tela educativa prévia com solicitação explícita de permissão.
- **Controle Total**: Switches instantâneos para pausar acessibilidade, HUD ou TTS a qualquer momento.
