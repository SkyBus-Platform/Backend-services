-- Flyway's own metadata table (flyway_schema_history) is also created
-- inside the "users" schema, keeping it fully isolated.

CREATE SCHEMA IF NOT EXISTS skybus_user_schema;

CREATE TABLE IF NOT EXISTS skybus_user_schema.skybus_user (
                                           id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL,
    password_hash TEXT         NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    phone         VARCHAR(20),
    role          VARCHAR(20)  NOT NULL DEFAULT 'PASSENGER',
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('PASSENGER', 'ADMIN'))
    );

CREATE INDEX idx_user_email     ON skybus_user_schema.skybus_user (email);
CREATE INDEX idx_user_role      ON skybus_user_schema.skybus_user (role);
CREATE INDEX idx_user_is_active ON skybus_user_schema.skybus_user (is_active);

-- ── Trigger: auto-update updated_at ──────────────────────────
CREATE OR REPLACE FUNCTION skybus_user_schema.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON skybus_user_schema.skybus_user
    FOR EACH ROW EXECUTE FUNCTION skybus_user_schema.set_updated_at();

-- ── Refresh tokens ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS skybus_user_schema.skybus_refresh_token (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL,
    token      VARCHAR(512) NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token  UNIQUE (token),
    CONSTRAINT fk_refresh_user   FOREIGN KEY (user_id)
    REFERENCES skybus_user_schema.skybus_user (id) ON DELETE CASCADE
    );

CREATE INDEX idx_refresh_token_user_id ON skybus_user_schema.skybus_refresh_token (user_id);
CREATE INDEX idx_refresh_token_token   ON skybus_user_schema.skybus_refresh_token (token);
CREATE INDEX idx_refresh_token_expires ON skybus_user_schema.skybus_refresh_token (expires_at);

COMMENT ON TABLE  skybus_user_schema.skybus_user          IS 'User accounts — owned by user-service';
COMMENT ON TABLE  skybus_user_schema.skybus_refresh_token IS 'JWT refresh token registry — one row per active session';
COMMENT ON COLUMN skybus_user_schema.skybus_user.password_hash IS 'BCrypt hash, cost factor >= 12. Never store plain text.';