package com.ifsul.backend.aula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FluxoEventosTest {
    @LocalServerPort
    private int porta;

    @Autowired
    private JsonMapper jsonMapper;

    private final HttpClient cliente = HttpClient.newHttpClient();

    private HttpResponse<String> requisicao(String metodo, String caminho, String corpo) throws Exception {
        HttpRequest.Builder pedido = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho));
        if (corpo == null) {
            pedido.method(metodo, HttpRequest.BodyPublishers.noBody());
        } else {
            pedido.header("Content-Type", "application/json")
                    .method(metodo, HttpRequest.BodyPublishers.ofString(corpo));
        }
        return cliente.send(pedido.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void fluxoEExportacoes() throws Exception {
        HttpResponse<String> usuario = requisicao("POST", "/usuarios",
                "{\"nome\":\"Ana & <Bia> São\",\"email\":\"ana@example.com\",\"idade\":20}");
        assertEquals(201, usuario.statusCode());
        long usuarioId = jsonMapper.readTree(usuario.body()).get("id").asLong();

        String dadosEvento = "{\"titulo\":\"Oficina & <API>\",\"descricao\":\"Teste\","
                + "\"data\":\"2026-11-25\",\"local\":\"Lab\",\"capacidade\":1}";
        HttpResponse<String> evento = requisicao("POST", "/eventos", dadosEvento);
        assertEquals(201, evento.statusCode());
        long eventoId = jsonMapper.readTree(evento.body()).get("id").asLong();
        String inscricao = "{\"usuarioId\":" + usuarioId + ",\"eventoId\":" + eventoId + "}";

        HttpResponse<String> vazio = requisicao("GET", "/eventos/" + eventoId + "/exportar/xml", null);
        assertEquals(200, vazio.statusCode());
        assertEquals(0, xml(vazio.body()).getElementsByTagName("participante").getLength());
        assertEquals(0, jsonMapper.readTree(requisicao("GET",
                "/eventos/" + eventoId + "/exportar/json", null).body()).size());

        HttpResponse<String> cadastro = requisicao("POST", "/inscricoes", inscricao);
        assertEquals(201, cadastro.statusCode());
        long inscricaoId = jsonMapper.readTree(cadastro.body()).get("id").asLong();
        assertEquals(409, requisicao("POST", "/inscricoes", inscricao).statusCode());
        HttpResponse<String> outroUsuario = requisicao("POST", "/usuarios",
                "{\"nome\":\"Bruno\",\"email\":\"bruno@example.com\",\"idade\":21}");
        assertEquals(201, outroUsuario.statusCode());
        long outroUsuarioId = jsonMapper.readTree(outroUsuario.body()).get("id").asLong();
        String outraInscricao = "{\"usuarioId\":" + outroUsuarioId + ",\"eventoId\":" + eventoId + "}";
        assertEquals(409, requisicao("POST", "/inscricoes", outraInscricao).statusCode());
        assertEquals(200, requisicao("PUT", "/eventos/" + eventoId,
                dadosEvento.replace("\"capacidade\":1", "\"capacidade\":2")).statusCode());
        HttpResponse<String> segundoCadastro = requisicao("POST", "/inscricoes", outraInscricao);
        assertEquals(201, segundoCadastro.statusCode());
        assertEquals(409, requisicao("PUT", "/eventos/" + eventoId, dadosEvento).statusCode());
        long segundaInscricaoId = jsonMapper.readTree(segundoCadastro.body()).get("id").asLong();
        assertEquals(204, requisicao("DELETE", "/inscricoes/" + segundaInscricaoId, null).statusCode());
        assertEquals(204, requisicao("DELETE", "/usuarios/" + outroUsuarioId, null).statusCode());
        assertEquals(409, requisicao("DELETE", "/usuarios/" + usuarioId, null).statusCode());
        assertEquals(409, requisicao("DELETE", "/eventos/" + eventoId, null).statusCode());
        assertEquals(400, requisicao("PUT", "/eventos/" + eventoId,
                dadosEvento.replace("\"capacidade\":1", "\"capacidade\":0")).statusCode());

        HttpResponse<String> json = requisicao("GET", "/eventos/" + eventoId + "/exportar/json", null);
        HttpResponse<String> xml = requisicao("GET", "/eventos/" + eventoId + "/exportar/xml", null);
        assertEquals(200, json.statusCode());
        assertEquals(200, xml.statusCode());
        assertTrue(json.headers().firstValue("content-type").orElse("").contains("application/json"));
        assertTrue(xml.headers().firstValue("content-type").orElse("").contains("application/xml"));
        assertTrue(json.headers().firstValue("content-disposition").orElse("").contains("participantes.json"));
        assertTrue(xml.headers().firstValue("content-disposition").orElse("").contains("participantes.xml"));
        JsonNode participante = jsonMapper.readTree(json.body()).get(0);
        Document documento = xml(xml.body());
        assertEquals("participantes", documento.getDocumentElement().getTagName());
        assertEquals(1, documento.getElementsByTagName("participante").getLength());
        for (String campo : new String[] {"id", "usuarioId", "usuarioNome", "eventoId", "eventoTitulo"}) {
            assertEquals(participante.get(campo).asText(), documento.getElementsByTagName(campo).item(0).getTextContent());
        }
        assertEquals(LocalDateTime.parse(participante.get("dataInscricao").asText()),
                LocalDateTime.parse(documento.getElementsByTagName("dataInscricao").item(0).getTextContent()));
        assertEquals("Ana & <Bia> São", documento.getElementsByTagName("usuarioNome").item(0).getTextContent());
        assertEquals("Oficina & <API>", documento.getElementsByTagName("eventoTitulo").item(0).getTextContent());

        assertEquals(404, requisicao("GET", "/eventos/999999/exportar/json", null).statusCode());
        assertEquals(404, requisicao("GET", "/eventos/999999/exportar/xml", null).statusCode());
        assertEquals(204, requisicao("DELETE", "/inscricoes/" + inscricaoId, null).statusCode());
        assertEquals(204, requisicao("DELETE", "/eventos/" + eventoId, null).statusCode());
        assertEquals(204, requisicao("DELETE", "/usuarios/" + usuarioId, null).statusCode());
    }

    @Test
    void atualizarUsuariosEEventos() throws Exception {
        HttpResponse<String> usuario = requisicao("POST", "/usuarios",
                "{\"nome\":\"Antes\",\"email\":\"antes@example.com\",\"idade\":20}");
        assertEquals(201, usuario.statusCode());
        long usuarioId = jsonMapper.readTree(usuario.body()).get("id").asLong();
        HttpResponse<String> outroUsuario = requisicao("POST", "/usuarios",
                "{\"nome\":\"Outro\",\"email\":\"outro@example.com\",\"idade\":21}");
        assertEquals(201, outroUsuario.statusCode());
        long outroUsuarioId = jsonMapper.readTree(outroUsuario.body()).get("id").asLong();

        assertEquals(409, requisicao("PUT", "/usuarios/" + usuarioId,
                "{\"nome\":\"Alterado\",\"email\":\"outro@example.com\",\"idade\":22}").statusCode());
        assertEquals(200, requisicao("PUT", "/usuarios/" + usuarioId,
                "{\"nome\":\"Alterado\",\"email\":\"alterado@example.com\",\"idade\":22}").statusCode());
        JsonNode usuarioAtualizado = jsonMapper.readTree(requisicao("GET", "/usuarios/" + usuarioId, null).body());
        assertEquals("Alterado", usuarioAtualizado.get("nome").asText());
        assertEquals("alterado@example.com", usuarioAtualizado.get("email").asText());
        assertEquals(22, usuarioAtualizado.get("idade").asInt());

        HttpResponse<String> evento = requisicao("POST", "/eventos",
                "{\"titulo\":\"Antes\",\"descricao\":\"Original\",\"data\":\"2026-11-25\",\"local\":\"Sala 1\",\"capacidade\":2}");
        assertEquals(201, evento.statusCode());
        long eventoId = jsonMapper.readTree(evento.body()).get("id").asLong();
        assertEquals(200, requisicao("PUT", "/eventos/" + eventoId,
                "{\"titulo\":\"Depois\",\"descricao\":\"Nova\",\"data\":\"2026-12-01\",\"local\":\"Sala 2\",\"capacidade\":3}").statusCode());
        JsonNode eventoAtualizado = jsonMapper.readTree(requisicao("GET", "/eventos/" + eventoId, null).body());
        assertEquals("Depois", eventoAtualizado.get("titulo").asText());
        assertEquals("Nova", eventoAtualizado.get("descricao").asText());
        assertEquals("2026-12-01", eventoAtualizado.get("data").asText());
        assertEquals("Sala 2", eventoAtualizado.get("local").asText());
        assertEquals(3, eventoAtualizado.get("capacidade").asInt());

        assertEquals(204, requisicao("DELETE", "/eventos/" + eventoId, null).statusCode());
        assertEquals(204, requisicao("DELETE", "/usuarios/" + usuarioId, null).statusCode());
        assertEquals(204, requisicao("DELETE", "/usuarios/" + outroUsuarioId, null).statusCode());
    }

    @Test
    void paginasEModulosEstaoDisponiveis() throws Exception {
        for (String caminho : new String[] {"/", "/usuarios.html", "/eventos.html", "/inscricoes.html",
                "/js/api.js", "/js/interface.js", "/js/lista-eventos.js", "/js/usuarios.js",
                "/js/eventos.js", "/js/inscricoes.js"}) {
            assertEquals(200, requisicao("GET", caminho, null).statusCode(), caminho);
        }
    }

    private Document xml(String conteudo) throws Exception {
        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return fabrica.newDocumentBuilder().parse(new InputSource(new StringReader(conteudo)));
    }
}
