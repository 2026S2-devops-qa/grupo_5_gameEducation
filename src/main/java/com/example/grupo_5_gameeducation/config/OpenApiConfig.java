package com.example.grupo_5_gameeducation.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger do Core, acessado pelo gateway (http://localhost:8080/swagger-ui.html).
 *
 * Todas as rotas /api/** exigem JWT no gateway (RNF03), entao o Swagger ganha o botao
 * "Authorize": cole o token devolvido por POST /auth/login. O Core em si nao valida o JWT:
 * quem valida e o gateway, que repassa a identidade nos headers X-User-*.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "GameEducation Core", version = "af",
                description = "Core de negocio (monolito modular). Acesso pelo API Gateway com JWT."),
        security = @SecurityRequirement(name = OpenApiConfig.BEARER))
@SecurityScheme(name = OpenApiConfig.BEARER, type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

    static final String BEARER = "bearer";
}
