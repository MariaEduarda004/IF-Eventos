package com.ifsul.backend.aula.controller;

import com.ifsul.backend.aula.dto.InscricaoRequest;
import com.ifsul.backend.aula.dto.InscricaoResponse;
import com.ifsul.backend.aula.service.InscricaoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inscricoes")
public class InscricaoController {

    private final InscricaoService service;

    public InscricaoController(InscricaoService service) {

        this.service = service;
    }

    @GetMapping("/evento/{eventoId}")
    public List<InscricaoResponse> listarPorEvento(@PathVariable Long eventoId) {
        return service.listarPorEvento(eventoId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InscricaoResponse cadastrar(@Valid @RequestBody InscricaoRequest dados) {

        return service.cadastrar(dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Long id) {

        service.cancelar(id);
    }
}
