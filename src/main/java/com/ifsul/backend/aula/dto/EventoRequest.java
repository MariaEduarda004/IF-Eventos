package com.ifsul.backend.aula.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record EventoRequest(
    @NotBlank(message = "Título é obrigatório")
    String titulo,

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    String descricao,

    @NotNull(message = "Data é obrigatória")
    LocalDate data,

    @NotBlank(message = "Local é obrigatório")
    String local,

    @NotNull(message = "Capacidade é obrigatória")
    @Min(value = 1, message = "Capacidade deve ser maior que zero")
    Integer capacidade
) {
}
