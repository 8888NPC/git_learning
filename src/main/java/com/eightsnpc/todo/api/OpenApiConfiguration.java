package com.eightsnpc.todo.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI taskApiDocumentation() {
        return new OpenAPI().info(new Info()
                .title("任务清单 API")
                .version("v1")
                .description("任务清单的健康检查和任务管理接口"));
    }
}
