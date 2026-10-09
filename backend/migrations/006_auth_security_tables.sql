-- =====================================================================
-- ROTA IQ — MIGRAÇÃO 006: SEGURANÇA DE AUTENTICAÇÃO E SESSÃO (PERSISTÊNCIA SUPABASE)
-- Resolve P1-004: Persistência serverless de logout real, tokens revogados,
-- recuperação de senha (tokens descartáveis) e conformidade LGPD.
-- =====================================================================

-- 1. Tokens Revogados (Logout Real em Ambiente Serverless / Multi-instância)
CREATE TABLE IF NOT EXISTS revoked_tokens (
    token_hash VARCHAR(64) PRIMARY KEY,
    revoked_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_revoked_tokens_date ON revoked_tokens(revoked_at);

-- 2. Recuperação de Senha (Tokens descartáveis com expiração)
CREATE TABLE IF NOT EXISTS password_resets (
    token VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_password_resets_email ON password_resets(email);
CREATE INDEX IF NOT EXISTS idx_password_resets_used ON password_resets(used);

-- 3. Verificação de E-mail
CREATE TABLE IF NOT EXISTS email_verifications (
    email VARCHAR(255) PRIMARY KEY,
    verification_token VARCHAR(255) NOT NULL,
    verified BOOLEAN DEFAULT FALSE NOT NULL,
    verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- 4. Trilha de Auditoria LGPD — Aceite de Termos de Uso e Política de Privacidade
CREATE TABLE IF NOT EXISTS terms_acceptances (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    terms_version VARCHAR(20) NOT NULL,
    privacy_version VARCHAR(20) NOT NULL,
    accepted_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    ip_address VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_terms_acceptances_user ON terms_acceptances(user_id);
