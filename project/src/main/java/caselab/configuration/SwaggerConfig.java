package caselab.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
    info = @Info(
        title = "Проект Green СР 2024 Java API",
        description = "Система ЭДО", version = "1.0.0",
        contact = @Contact(
            name = "GitHub",
            url = "https://github.com/Alexandr-Kokorin/CaseLabProject"
        )
    )
)
@SecurityScheme(
    name = "JWT",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
public class SwaggerConfig { }
