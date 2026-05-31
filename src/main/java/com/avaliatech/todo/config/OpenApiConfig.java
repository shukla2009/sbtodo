package com.avaliatech.todo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI todoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Todo API")
                        .description("Spring Boot 4 TODO CRUD API")
                        .version("v1")
                        .contact(new Contact()
                                .name("Avaliatech")
                                .email("support@avaliatech.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
