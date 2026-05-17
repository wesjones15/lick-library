package org.jones.licklibrary.core.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public SchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute(
            "ALTER TABLE lick ADD COLUMN IF NOT EXISTS auto_imported BOOLEAN DEFAULT FALSE"
        );
        jdbcTemplate.execute(
            "UPDATE lick SET auto_imported = FALSE WHERE auto_imported IS NULL"
        );
    }
}
