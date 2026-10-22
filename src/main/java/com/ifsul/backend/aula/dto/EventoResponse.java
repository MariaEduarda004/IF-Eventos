package com.ifsul.backend.aula.dto;

import java.time.LocalDate;

public record EventoResponse(
    Long id,
    String titulo,
    String descricao,
    LocalDate data,
    String local,
    Integer capacidade,
    Long inscritos,
    Long vagasDisponiveis
) {
}
