-- Inserts a default admin account for first-time setup.
--
-- Password: Admin@1234
-- BCrypt hash (cost 12) — generated offline, never plain text in migrations.
--
-- IMPORTANT: Change this password immediately after first login in any
-- non-local environment. Rotate by calling PATCH /api/users/me or
-- directly updating the hash with a new bcrypt value.
--
-- To generate a new hash:
--   Java : new BCryptPasswordEncoder(12).encode("YourNewPassword")
--   CLI  : htpasswd -bnBC 12 "" YourPassword | tr -d ':\n'

INSERT INTO skybus_user_schema.skybus_user (
    id,
    email,
    password_hash,
    first_name,
    last_name,
    role,
    is_active,
    created_at,
    updated_at
)
VALUES (
           gen_random_uuid(),
           'admin@skybus.com',
           '$2a$12$eLqRiMqMjGmTSGWGk3MoZuRfXXwBIXKQsFGfXpnuSXBaQ0jg4dVaO',
           'System',
           'Admin',
           'ADMIN',
           TRUE,
           NOW(),
           NOW()
       )
    ON CONFLICT (email) DO NOTHING;  -- safe to re-run; won't duplicate if already exists