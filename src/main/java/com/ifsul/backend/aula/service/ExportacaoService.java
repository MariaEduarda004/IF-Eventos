
package com.ifsul.backend.aula.service;

import com.ifsul.backend.aula.dto.InscricaoResponse;
import com.ifsul.backend.aula.dto.ParticipantesXml;
import java.util.List;
import org.springframework.stereotype.Service;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlWriteFeature;

@Service
public class ExportacaoService {

    private final InscricaoService inscricaoService;
    private final JsonMapper jsonMapper;
    private final XmlMapper xmlMapper;

    public ExportacaoService(InscricaoService inscricaoService, JsonMapper jsonMapper) {
        this.inscricaoService = inscricaoService;
        this.jsonMapper = jsonMapper;
        this.xmlMapper = XmlMapper.builder()
                .enable(XmlWriteFeature.WRITE_XML_DECLARATION)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    public String exportarParticipantesJson(Long eventoId) {
        return jsonMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(inscricaoService.listarPorEvento(eventoId));
    }

    public String exportarParticipantesXml(Long eventoId) {
        List<InscricaoResponse> participantes = inscricaoService.listarPorEvento(eventoId);
        return xmlMapper.writeValueAsString(new ParticipantesXml(participantes));
    }
}
