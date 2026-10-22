
package com.ifsul.backend.aula.service;

import com.ifsul.backend.aula.dto.InscricaoResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
public class ExportacaoService {

    private final InscricaoService inscricaoService;
    private final JsonMapper jsonMapper;

    public ExportacaoService(InscricaoService inscricaoService, JsonMapper jsonMapper) {
        this.inscricaoService = inscricaoService;
        this.jsonMapper = jsonMapper;
    }

    public String exportarParticipantesJson(Long eventoId) {

        List<InscricaoResponse> participantes =
                inscricaoService.listarPorEvento(eventoId);

        String json = jsonMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(participantes);

        return json;
    }
}
