package com.example.authservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authServiceOpenApi(
            @Value("${app.api-docs.version}") String apiVersion) {
        return new OpenAPI()
                .info(new Info()
                        .title("Auth Service API")
                        .version(apiVersion)
                        .description("Registration, login, and JWT issuance (aligns with project SemVer in pom.xml)."));
    }
}
