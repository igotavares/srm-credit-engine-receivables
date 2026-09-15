package br.com.ibeans.receivables.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "API de Recebíveis",
        version = "v1",
        description = "Cadastro de recebíveis, configuração de taxas, simulação de precificação, liquidação e relatórios. "
                + "Valores decimais nas respostas são strings para preservar precisão. Datas e horas são informadas sem fuso horário."
))
public class OpenApiConfiguration {
}
