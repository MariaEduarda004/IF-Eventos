package com.ifsul.backend.aula.controller;

import com.ifsul.backend.aula.dto.EventoRequest;
import com.ifsul.backend.aula.dto.EventoResponse;
import com.ifsul.backend.aula.service.EventoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.ifsul.backend.aula.service.ExportacaoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/eventos")
public class EventoController {

    private final EventoService service;
    private final ExportacaoService exportacaoService;

    public EventoController(EventoService service, ExportacaoService exportacaoService) {
        this.service = service;
        this.exportacaoService = exportacaoService;
    }

    @GetMapping
    public List<EventoResponse> listar() {

        return service.listar();
    }

    @GetMapping("/{id}")
    public EventoResponse buscar(@PathVariable Long id) {

        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventoResponse cadastrar(@Valid @RequestBody EventoRequest dados) {

        return service.cadastrar(dados);
    }

    @PutMapping("/{id}")
    public EventoResponse atualizar(@PathVariable Long id,  @Valid @RequestBody EventoRequest dados) {

        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {

        service.excluir(id);
    }


    @GetMapping("/{id}/exportar/json")
    public ResponseEntity<String> exportarParticipantes(@PathVariable Long id) {
        String json = exportacaoService.exportarParticipantesJson(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"participantes.json\""
                )
                .body(json);
    }
}
