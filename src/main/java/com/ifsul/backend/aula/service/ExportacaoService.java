
package com.ifsul.backend.aula.service;

import com.ifsul.backend.aula.dto.InscricaoResponse;
import java.io.StringWriter;
import java.util.List;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
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
        return jsonMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(inscricaoService.listarPorEvento(eventoId));
    }

    public String exportarParticipantesXml(Long eventoId) {
        List<InscricaoResponse> participantes = inscricaoService.listarPorEvento(eventoId);
        StringWriter conteudo = new StringWriter();
        try {
            XMLStreamWriter xml = XMLOutputFactory.newFactory().createXMLStreamWriter(conteudo);
            xml.writeStartDocument("UTF-8", "1.0");
            xml.writeStartElement("participantes");
            for (InscricaoResponse participante : participantes) {
                xml.writeStartElement("participante");
                escreverElemento(xml, "id", participante.id().toString());
                escreverElemento(xml, "usuarioId", participante.usuarioId().toString());
                escreverElemento(xml, "usuarioNome", participante.usuarioNome());
                escreverElemento(xml, "eventoId", participante.eventoId().toString());
                escreverElemento(xml, "eventoTitulo", participante.eventoTitulo());
                escreverElemento(xml, "dataInscricao", participante.dataInscricao().toString());
                xml.writeEndElement();
            }
            xml.writeEndElement();
            xml.writeEndDocument();
            xml.close();
            return conteudo.toString();
        } catch (XMLStreamException erro) {
            throw new IllegalStateException("Não foi possível gerar o XML de participantes", erro);
        }
    }

    private void escreverElemento(XMLStreamWriter xml, String nome, String valor) throws XMLStreamException {
        if (valor.codePoints().anyMatch(c -> c != 0x9 && c != 0xA && c != 0xD
                && !(c >= 0x20 && c <= 0xD7FF)
                && !(c >= 0xE000 && c <= 0xFFFD)
                && !(c >= 0x10000 && c <= 0x10FFFF))) {
            throw new XMLStreamException("O campo " + nome + " contém caracteres inválidos para XML");
        }
        xml.writeStartElement(nome);
        xml.writeCharacters(valor);
        xml.writeEndElement();
    }
}
