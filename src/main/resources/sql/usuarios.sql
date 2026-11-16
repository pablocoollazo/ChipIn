TRUNCATE usuario, grupo, miembro_grupo RESTART IDENTITY CASCADE;

-- Contraseña de los tres: 1234
INSERT INTO usuario (nombre, email, password_hash, fecha_registro)
VALUES ('John Doe', 'john.doe@example.com', '$2a$10$OBYBgMbcoW0z.RhksT7rZu6rmSKEdqmz3ba4a/dpyyVHouY1pJdQC', now()),
       ('Jane Smith', 'jane.smith@example.com', '$2a$10$OBYBgMbcoW0z.RhksT7rZu6rmSKEdqmz3ba4a/dpyyVHouY1pJdQC', now()),
       ('Alice Johnson', 'alice.johnson@example.com', '$2a$10$OBYBgMbcoW0z.RhksT7rZu6rmSKEdqmz3ba4a/dpyyVHouY1pJdQC',
        now());