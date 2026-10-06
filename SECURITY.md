# Segurança, Privacidade e Diretrizes de Conformidade

## 1. Princípios de Segurança e Privacidade

O **ROTA IQ** é projetado sob os princípios de **Privacy by Design** e estrita conformidade com as políticas do Google Play e a **LGPD (Lei Geral de Proteção de Dados - Lei 13.709/2018)**.

### Regras Fundamentais:
1. **Não capturar senhas ou credenciais**: O sistema nunca lê senhas, tokens de autenticação ou dados bancários de qualquer aplicativo.
2. **Não violar APIs privadas**: O ROTA IQ não realiza engenharia reversa de APIs fechadas da Uber ou 99.
3. **Não alterar aplicativos de terceiros**: Nenhum APK ou memória de processos de terceiros é alterada ou injetada.
4. **Processamento 100% Local (On-Device)**: Toda a análise inicial de custos, cálculo de notas e síntese de voz ocorrem localmente no dispositivo em menos de 2 milissegundos, sem envio de telemetria desnecessária a servidores.

---

## 2. Conformidade com o Android AccessibilityService (Fase 3)

O `RotaIqAccessibilityService` foi implementado respeitando as diretrizes estritas do Google Play Developer Policy:
- **Finalidade Declarada**: Auxílio de tomada de decisão e segurança ao volante para motoristas profissionais.
- **Escopo Restrito**: O serviço apenas monitora os pacotes específicos dos aplicativos de corrida (`com.ubercab.driver`, `com.taxis99`).
- **Filtro de Conteúdo**: O extrator de texto (`AccessibilityNodeExtractor`) apenas coleta nós visíveis que compõem o cartão de oferta (valor bruto, quilometragem, tempo estimado). Conteúdos de conversas particulares, chats com passageiros ou dados de pagamento são sumariamente ignorados.
- **Transparência e Consentimento**: O aplicativo conta com a tela `AutomationHubScreen` que explica de forma clara ao motorista a razão exata da necessidade do serviço antes de encaminhá-lo para a tela nativa de acessibilidade do Android.
- **Controle Total pelo Usuário**: O motorista pode pausar ou desativar o serviço de acessibilidade, o HUD flutuante ou o sintetizador de voz a qualquer instante através de switches no cockpit.

---

## 3. Armazenamento Seguro e Criptografia

- Dados de perfil do motorista e parâmetros de custos do carro ficam salvos no banco local Room criptografável via SQLiteCipher / EncryptedSharedPreferences.
- Toda a comunicação futura de sincronização com o banco remoto PostgreSQL utiliza **TLS 1.3**.

---

## 4. Direitos do Titular de Dados (LGPD)

O sistema conta com especificações para:
- **Exclusão Completa**: Opção no painel de configurações para apagar todo o histórico de avaliações e perfil do veículo local e remotamente.
- **Exportação de Dados**: Possibilidade de exportar dados em formato legível (JSON/CSV) de histórico de corridas e custos calculados.
