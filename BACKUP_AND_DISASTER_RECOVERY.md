# ROTA IQ — POLÍTICA DE BACKUP E PLANO DE RECUPERAÇÃO DE DESASTRES (DISASTER RECOVERY)

**Versão:** 1.0.0 (Fase 9 • Preparação para Produção)  
**Data:** 09/10/2026  
**Responsável:** Arquiteto de Software & Administrador de Banco de Dados  

---

## 1. OBJETIVOS E MÉTRICAS OPERACIONAIS

Em caso de falha catastrófica de infraestrutura, exclusão acidental ou indisponibilidade de datacenter, as seguintes métricas devem ser rigorosamente atendidas:

| Métrica | Meta Operacional | Descrição |
|:---|:---:|:---|
| **RPO (Recovery Point Objective)** | **< 1 hora** | Tolerância máxima de perda de dados entre o último backup e o incidente |
| **RTO (Recovery Time Objective)** | **< 30 minutos** | Tempo máximo decorrido até o restabelecimento completo das operações da API |

---

## 2. ESTRATÉGIA DE BACKUP MULTICAMADAS

### A. Point-In-Time Recovery (PITR) Contínuo — Supabase
- **Mecanismo:** Write-Ahead Logging (WAL) contínuo do PostgreSQL arquivado em storage redundante.
- **Janela de Retenção:** 7 dias (Plano Pro) ou 30 dias (Plano Enterprise).
- **Recuperação:** Permite restaurar o banco para **qualquer segundo específico** do passado.

### B. Backup Lógico Diário Automatizado (`pg_dump`)
- **Frequência:** Executado diariamente às 03:00 UTC (horário de menor tráfego de motoristas).
- **Formato:** PostgreSQL Custom Binary (`.dump`), comprimido com encriptação AES-256 em trânsito e repouso.
- **Destino:** Armazenamento externo independente (Amazon S3 / Google Cloud Storage com versionamento ativado).

#### Script de Execução Diária:
```bash
#!/bin/bash
set -e

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_DIR="/var/backups/rota_iq"
FILENAME="rota_iq_prod_${TIMESTAMP}.dump"

echo "[$(date)] Iniciando backup do banco de dados ROTA IQ..."
mkdir -p "$BACKUP_DIR"

# Executa pg_dump com formato custom comprimido
PGPASSWORD="${SUPABASE_DB_PASSWORD}" pg_dump \
  --host="db.${SUPABASE_PROJECT_REF}.supabase.co" \
  --port="5432" \
  --username="postgres" \
  --dbname="postgres" \
  --format=c \
  --blobs \
  --verbose \
  --file="${BACKUP_DIR}/${FILENAME}"

echo "[$(date)] Backup local gerado com sucesso: ${FILENAME}"

# Upload seguro para storage frio (S3/GCS)
aws s3 cp "${BACKUP_DIR}/${FILENAME}" "s3://rota-iq-database-backups/daily/${FILENAME}" \
  --sse aws:kms

# Purga backups locais com mais de 7 dias
find "$BACKUP_DIR" -type f -name "*.dump" -mtime +7 -delete

echo "[$(date)] Rotina de backup concluída com sucesso."
```

---

## 3. PLAYBOOK DE RECUPERAÇÃO DE DESASTRES (PASSO A PASSO)

### Cenário 1: Restauração por PITR no Supabase (Exclusão Inadvertida)
1. Acesse o **Supabase Dashboard** > **Settings** > **Database** > **Backups**.
2. Selecione a opção **Point in Time Recovery**.
3. Defina a data e o minuto imediatamente anterior ao incidente (ex: `2026-10-09 15:42:00 UTC`).
4. Clique em **Restore Database to Point in Time**.
5. Aguarde o provisionamento (tipicamente entre 5 e 15 minutos).
6. Execute o endpoint `/api/v1/health` para validar a latência e contagem de registros.

---

### Cenário 2: Restauração Total em Nova Instância (Catástrofe de Infraestrutura)
Se a instância primária for perdida ou for necessário migrar de emergência para outro servidor:

1. **Provisionar Nova Instância PostgreSQL 15+**:
   - Pode ser um novo projeto Supabase, RDS AWS ou VPS com PostgreSQL dedicado.

2. **Aplicar Estrutura Base e Migrations**:
   Execute as migrações sequenciais localizadas em `backend/migrations/`:
   ```bash
   psql -h <NOVO_HOST> -U postgres -d postgres -f backend/migrations/001_initial_schema.sql
   psql -h <NOVO_HOST> -U postgres -d postgres -f backend/migrations/002_financial_and_sync.sql
   psql -h <NOVO_HOST> -U postgres -d postgres -f backend/migrations/003_commercial_subscriptions_and_admin.sql
   psql -h <NOVO_HOST> -U postgres -d postgres -f backend/migrations/004_admin_backoffice_real.sql
   psql -h <NOVO_HOST> -U postgres -d postgres -f backend/migrations/005_push_campaigns_and_devices.sql
   psql -h <NOVO_HOST> -U postgres -d postgres -f backend/migrations/006_auth_security_tables.sql
   psql -h <NOVO_HOST> -U postgres -d postgres -f backend/migrations/007_telemetry_retention_purge.sql
   ```

3. **Restaurar os Dados a Partir do Arquivo de Dump**:
   ```bash
   pg_restore --host="<NOVO_HOST>" \
              --port="5432" \
              --username="postgres" \
              --dbname="postgres" \
              --data-only \
              --disable-triggers \
              --verbose "rota_iq_prod_latest.dump"
   ```

4. **Atualizar Variáveis de Ambiente no Backend (Vercel)**:
   - `SUPABASE_URL`: Nova URL da API.
   - `SUPABASE_KEY`: Nova chave de serviço.
   - Disparar Redeploy do Backend.

5. **Validação E2E de Saúde do Sistema**:
   ```bash
   curl -s "https://rotaiq-puce.vercel.app/api/v1/health" | jq .
   ```
   Critério de aceite: `status: "HEALTHY"`, `database: "connected"`.

---

## 4. AUDITORIA E TESTE DE RESTAURAÇÃO
- **Simulação Periódica:** Um exercício de restauração em ambiente de homologação (`staging`) deve ser executado no primeiro dia de cada mês.
- **Validação de Integridade:** Testes unitários do backend (`python -m pytest tests -v`) devem rodar contra a base restaurada para atestar integridade referencial.
