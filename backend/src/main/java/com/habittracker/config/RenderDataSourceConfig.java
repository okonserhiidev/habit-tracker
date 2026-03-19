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
 * Converts Render's DATABASE_URL (postgres://user:pass@host:port/db)
 * to a valid JDBC URL for Spring Boot.
 */
@Configuration
@Profile("prod")
public class RenderDataSourceConfig {

    @Value("${DATABASE_URL}")
    private String databaseUrl;

    @Bean
    public DataSource dataSource() {
        URI dbUri = URI.create(databaseUrl.replace("postgres://", "postgresql://"));

        String host = dbUri.getHost();
        int port = dbUri.getPort() == -1 ? 5432 : dbUri.getPort();
        String dbName = dbUri.getPath().substring(1); // strip leading /
        String userInfo = dbUri.getUserInfo();
        String username = userInfo.split(":")[0];
        String password = userInfo.contains(":") ? userInfo.substring(userInfo.indexOf(':') + 1) : "";

        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(30_000);
        config.addDataSourceProperty("sslmode", "require");

        return new HikariDataSource(config);
    }
}
