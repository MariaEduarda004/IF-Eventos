package com.ifsul.backend.aula.dto;

import java.time.LocalDateTime;

public record InscricaoResponse(
    Long id,
    Long usuarioId,
    String usuarioNome,
    Long eventoId,
    String eventoTitulo,
    LocalDateTime dataInscricao
) {
}
