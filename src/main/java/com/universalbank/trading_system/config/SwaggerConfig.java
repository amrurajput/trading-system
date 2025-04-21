package com.universalbank.trading_system.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI tradingSystemOpenAPI() {
        return new OpenAPI()
                // Optionally list your servers (e.g. dev, prod)
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Local development server"))
                // Main API info section
                .info(new Info()
                        .title("Universal Bank Trading System API")
                        .description("APIs for placing, managing, and executing trading orders")
                        .version("1.0.0")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@universalbank.com")
                                .url("https://universalbank.com/support"))
                )
                // External docs, e.g. a reference guide
                .externalDocs(new ExternalDocumentation()
                        .description("Project GitHub Repository")
                        .url("https://github.com/universalbank/trading-system"));
    }
}
