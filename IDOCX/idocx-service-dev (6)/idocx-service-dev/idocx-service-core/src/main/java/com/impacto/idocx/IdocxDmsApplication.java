package com.impacto.idocx;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@OpenAPIDefinition(info = @Info(title = "workspace open API",
        version = "1.0.0",
        description = "workspace api"),
        servers = @Server(
                url = "http://localhost:8080"
        ))

public class IdocxDmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdocxDmsApplication.class, args);
    }

}
