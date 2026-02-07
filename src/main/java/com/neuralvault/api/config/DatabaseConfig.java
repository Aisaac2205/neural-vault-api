package com.neuralvault.api.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Slf4j
@Configuration
@Profile("prod")
public class DatabaseConfig {

    @Value("${DATABASE_URL}")
    private String databaseUrl;

    @Bean
    public DataSource dataSource() {
        log.info("Configuring database connection from DATABASE_URL");
        
        try {
            // Parse Railway's DATABASE_URL format: postgresql://user:pass@host:port/db
            // Remove the protocol prefix
            String withoutProtocol = databaseUrl.replaceFirst("^postgresql://", "");
            
            // Split user:pass and the rest
            int atIndex = withoutProtocol.indexOf('@');
            if (atIndex == -1) {
                throw new IllegalArgumentException("Invalid DATABASE_URL format: missing @ separator");
            }
            
            String credentials = withoutProtocol.substring(0, atIndex);
            String hostPart = withoutProtocol.substring(atIndex + 1);
            
            // Parse credentials
            int colonIndex = credentials.indexOf(':');
            if (colonIndex == -1) {
                throw new IllegalArgumentException("Invalid DATABASE_URL format: missing : in credentials");
            }
            
            String username = credentials.substring(0, colonIndex);
            String password = credentials.substring(colonIndex + 1);
            
            // Parse host:port/db
            int slashIndex = hostPart.indexOf('/');
            if (slashIndex == -1) {
                throw new IllegalArgumentException("Invalid DATABASE_URL format: missing / before database name");
            }
            
            String hostAndPort = hostPart.substring(0, slashIndex);
            String database = hostPart.substring(slashIndex + 1);
            
            // Parse host and port
            int portColonIndex = hostAndPort.lastIndexOf(':');
            String host;
            int port;
            
            if (portColonIndex == -1) {
                // No port specified, use default
                host = hostAndPort.toLowerCase(); // Convert to lowercase
                port = 5432;
            } else {
                host = hostAndPort.substring(0, portColonIndex).toLowerCase(); // Convert to lowercase
                port = Integer.parseInt(hostAndPort.substring(portColonIndex + 1));
            }
            
            // Convert to JDBC format: jdbc:postgresql://host:port/db
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            
            log.info("Connecting to PostgreSQL at {}:{}/{} with user {}", host, port, database, username);
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("org.postgresql.Driver");
            
            // Railway-specific optimizations
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(20000);
            config.setIdleTimeout(300000);
            config.setMaxLifetime(1200000);
            config.setLeakDetectionThreshold(60000);
            
            return new HikariDataSource(config);
            
        } catch (Exception e) {
            log.error("Failed to parse DATABASE_URL: {}", databaseUrl.replaceAll("://[^@]+@", "://****@"), e);
            throw new RuntimeException("Invalid DATABASE_URL format", e);
        }
    }
}
