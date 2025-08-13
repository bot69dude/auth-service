package com.vitasync.auth_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactoryOptions;
import static io.r2dbc.spi.ConnectionFactoryOptions.*;

import java.time.Duration;

@Configuration
public class R2DBCConfig {

    @Value("${DB_URL}")
    private String dbUrl;

    @Value("${DB_USERNAME}")
    private String dbUsername;

    @Value("${DB_PASSWORD}")
    private String dbPassword;

    @Bean
    public ConnectionFactory connectionFactory() {
        ConnectionFactoryOptions options = ConnectionFactoryOptions.builder()
            .option(DRIVER, "postgresql")
            .option(HOST, dbUrl.split(":")[0])
            .option(PORT, Integer.parseInt(dbUrl.split(":")[1].split("/")[0]))
            .option(DATABASE, dbUrl.split("/")[1])
            .option(USER, dbUsername)
            .option(PASSWORD, dbPassword)
            .option(SSL, true)
            .build();

        ConnectionFactory connectionFactory = ConnectionFactories.get(options);

        ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(connectionFactory)
            .maxIdleTime(Duration.ofMinutes(30))
            .initialSize(5)
            .maxSize(10)
            .build();

        return new ConnectionPool(configuration);
    }
}
