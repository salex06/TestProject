CREATE TABLE IF NOT EXISTS messages(
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT REFERENCES users(id),
    receiver_id BIGINT REFERENCES users(id),
    text TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);