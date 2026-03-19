package com.habittracker.cucumber;

import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseCleanup {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestContext ctx;

    @Before
    public void cleanDatabase() {
        // Disable referential integrity, truncate all tables, re-enable
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE habit_chain_items");
        jdbcTemplate.execute("TRUNCATE TABLE habit_chains");
        jdbcTemplate.execute("TRUNCATE TABLE achievements");
        jdbcTemplate.execute("TRUNCATE TABLE freeze_days");
        jdbcTemplate.execute("TRUNCATE TABLE habit_completions");
        jdbcTemplate.execute("TRUNCATE TABLE habits");
        jdbcTemplate.execute("TRUNCATE TABLE users");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        // Reset auto-increment counters
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE habits ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE habit_completions ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE freeze_days ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE achievements ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE habit_chains ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE habit_chain_items ALTER COLUMN id RESTART WITH 1");

        // Reset test context
        ctx.reset();
    }
}
