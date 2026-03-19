package com.habittracker.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Configures DataSource for Render deployment.
 * Handles two formats of DATABASE_URL:
 *   1. Already JDBC: jdbc:postgresql://host/db?user=...&password=...
 *   2. Render default: postgres://user:pass@host:port/db
 */
@Configuration
@Profile("prod")
public class RenderDataSourceConfig {

    @Value("${DATABASE_URL}")
    private String databaseUrl;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(30_000);
        config.setDriverClassName("org.postgresql.Driver");

        if (databaseUrl.startsWith("jdbc:")) {
            // Already in JDBC format — use as-is
            config.setJdbcUrl(databaseUrl);
        } else {
            // Render default format: postgres://user:pass@host:port/db
            String normalized = databaseUrl.replaceFirst("^postgres://", "postgresql://");
            URI dbUri = URI.create(normalized);

            String host = dbUri.getHost();
            int port = dbUri.getPort() == -1 ? 5432 : dbUri.getPort();
            String dbName = dbUri.getPath().substring(1);
            String userInfo = dbUri.getUserInfo();
            String username = userInfo.split(":")[0];
            String password = userInfo.contains(":") ? userInfo.substring(userInfo.indexOf(':') + 1) : "";

            config.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName));
            config.setUsername(username);
            config.setPassword(password);
            config.addDataSourceProperty("sslmode", "require");
        }

        return new HikariDataSource(config);
    }
}
