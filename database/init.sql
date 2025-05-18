CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS documents (
    content     TEXT  NOT NULL,
    embedding   vector(1536)
);