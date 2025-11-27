package com.pms.ingestion.Config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Trade Ingestion API",
                version = "1.0",
                description = "Admin APIs for Safe Store, Outbox, DLQ"
        )
)
public class SwaggerConfig {

}
