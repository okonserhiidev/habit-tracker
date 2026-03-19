-- Habit Chains (Habit Stacking)
CREATE TABLE habit_chains (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'CUSTOM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chains_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE habit_chain_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chain_id BIGINT NOT NULL,
    habit_id BIGINT NOT NULL,
    position INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chain_items_chain FOREIGN KEY (chain_id) REFERENCES habit_chains(id) ON DELETE CASCADE,
    CONSTRAINT fk_chain_items_habit FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE
);

-- Password reset token
ALTER TABLE users ADD COLUMN reset_token VARCHAR(255);
ALTER TABLE users ADD COLUMN reset_token_expires_at TIMESTAMP;
