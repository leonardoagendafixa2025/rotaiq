# Segurança, Privacidade e Diretrizes de Conformidade

## 1. Princípios de Segurança e Privacidade

O **ROTA IQ** é projetado sob os princípios de **Privacy by Design** e estrita conformidade com as políticas do Google Play e a **LGPD (Lei Geral de Proteção de Dados - Lei 13.709/2018)**.

### Regras Fundamentais:
1. **Não capturar senhas ou credenciais**: O sistema nunca lê senhas, tokens de autenticação ou dados bancários de qualquer aplicativo.
2. **Não violar APIs privadas**: O ROTA IQ não realiza engenharia reversa de APIs fechadas da Uber ou 99.
3. **Não alterar aplicativos de terceiros**: Nenhum APK ou memória de processos de terceiros é alterada ou injetada.
4. **Processamento Local**: Toda a análise inicial de custos e notas de corrida ocorre 100% no dispositivo (on-device), sem envio de telemetria desnecessária a servidores.

---

## 2. Conformidade com o Android AccessibilityService

Nas fases subsequentes (Fase 3):
- O uso da API de acessibilidade será estritamente restrito a leitura de nós textuais de ofertas de transporte no momento em que são exibidas na tela do motorista.
- **Consentimento Explícito**: O aplicativo apresentará uma tela prévia detalhada com permissão granular, explicando por que o serviço é necessário.
- **Chave Liga/Desliga**: O motorista poderá pausar ou desativar o serviço a qualquer instante no cockpit.
- **Filtro de Conteúdo**: Nenhum conteúdo que não seja explicitamente identificado como texto de oferta de viagem (ex: chats particulares, dados de pagamento) é processado ou armazenado.

---

## 3. Armazenamento Seguro e Criptografia

- Dados de identificação do motorista serão protegidos utilizando o **Android Keystore** para geração e proteção de chaves assimétricas.
- Persistência sensível no DataStore é cifrada através do `EncryptedSharedPreferences` / `Tink`.
- Na comunicação futura com o backend, será obrigatório o uso de **TLS 1.3** com **Certificate Pinning**.

---

## 4. Direitos do Titular de Dados (LGPD)

O sistema conta com especificações para:
- **Exclusão Completa**: Opção no painel de configurações para apagar todo o histórico de avaliações e perfil do veículo local e remotamente.
- **Exportação de Dados**: Possibilidade de exportar dados em formato legível (JSON/CSV) de histórico de corridas e custos calculados.
