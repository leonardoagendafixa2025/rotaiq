-- ============================================================================
-- ROTA IQ — POLÍTICA DE RETENÇÃO E EXPURGO LGPD DE TELEMETRIA (30 DIAS)
-- Em conformidade com o Art. 16 da Lei Geral de Proteção de Dados (Lei 13.709/2018)
-- ============================================================================

CREATE OR REPLACE FUNCTION purge_expired_telemetry(retention_days INT DEFAULT 30)
RETURNS TABLE (
    deleted_events BIGINT,
    purged_at TIMESTAMP WITH TIME ZONE
) LANGUAGE plpgsql AS $$
DECLARE
    cutoff TIMESTAMP WITH TIME ZONE;
    count_deleted BIGINT := 0;
BEGIN
    cutoff := NOW() - (retention_days || ' days')::INTERVAL;
    
    -- Deleta eventos da tabela oficial sanitizada
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'sanitized_telemetry_events') THEN
        WITH deleted AS (
            DELETE FROM sanitized_telemetry_events
            WHERE received_at < cutoff
            RETURNING id
        )
        SELECT COUNT(*) INTO count_deleted FROM deleted;
    END IF;

    RETURN QUERY SELECT count_deleted, NOW();
END;
$$;

COMMENT ON FUNCTION purge_expired_telemetry(INT) IS 'Expurga automaticamente registros de telemetria mais antigos que retention_days (padrão: 30 dias) em conformidade com o Art. 16 da LGPD.';
