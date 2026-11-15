package com.ifsul.backend.aula.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ifsul.backend.aula.dto.InscricaoResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class ExportacaoServiceTest {
    @Test
    void naoGeraXmlInvalidoParaCaracterDeControle() {
        InscricaoService inscricaoService = mock(InscricaoService.class);
        when(inscricaoService.listarPorEvento(1L)).thenReturn(List.of(
                new InscricaoResponse(1L, 1L, "Nome\u0001", 1L, "Evento", LocalDateTime.now())
        ));
        ExportacaoService service = new ExportacaoService(inscricaoService, JsonMapper.builder().build());

        assertThrows(RuntimeException.class, () -> service.exportarParticipantesXml(1L));
    }
}
