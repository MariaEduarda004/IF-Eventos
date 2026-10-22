package com.ifsul.backend.aula.dto;

import jakarta.validation.constraints.NotNull;

public record InscricaoRequest(
    @NotNull(message = "Selecione um usuário")
    Long usuarioId,

    @NotNull(message = "Selecione um evento")
    Long eventoId
) {
}
