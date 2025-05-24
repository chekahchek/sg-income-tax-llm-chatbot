CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS documents (
    title       TEXT  PRIMARY KEY,
    content     TEXT  NOT NULL,
    embedding   vector(1536),
    extract_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);