package se.moln.ecommerceintegration.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI userServiceOpenAPI() {
        // Define the security schemes
        var securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT Bearer token for authentication");

        var apiKeyScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-KEY")
                .description("API Key for authentication");

        return new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .version("v1")
                        .description("Authentication & User management endpoints for the e-commerce platform."))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .addSecurityItem(new SecurityRequirement().addList("apiKeyAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme)
                        .addSecuritySchemes("apiKeyAuth", apiKeyScheme))
                .servers(List.of(
                        new Server().url("https://userservice.drillbi.se").description("Production Server"),
                        new Server().url("http://localhost:8080/").description("Production Server")
                ));
    }
}