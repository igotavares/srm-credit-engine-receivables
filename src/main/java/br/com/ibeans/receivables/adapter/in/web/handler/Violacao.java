package br.com.ibeans.receivables.adapter.in.web.handler;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Violacao", description = "Uma violação de validação de campo ou de objeto.")
public record Violacao(
        @Schema(description = "Campo inválido ou nome do objeto para validações globais.", example = "rate")
        String campo,
        @Schema(description = "Código da constraint ou do erro de binding.", example = "NotNull")
        String constraint,
        @Schema(description = "Mensagem de validação no idioma configurado.", example = "não deve ser nulo")
        String mensagem
) {
}
