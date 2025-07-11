CREATE TABLE IF NOT EXISTS contacts(
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT REFERENCES users(id),
    contact_id BIGINT REFERENCES users(id)
);