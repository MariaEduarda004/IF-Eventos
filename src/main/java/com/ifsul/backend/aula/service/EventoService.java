package com.ifsul.backend.aula.service;

import com.ifsul.backend.aula.dto.EventoRequest;
import com.ifsul.backend.aula.dto.EventoResponse;
import com.ifsul.backend.aula.entity.Evento;
import com.ifsul.backend.aula.exception.RecursoNaoEncontradoException;
import com.ifsul.backend.aula.exception.RegraDeNegocioException;
import com.ifsul.backend.aula.repository.EventoRepository;
import com.ifsul.backend.aula.repository.InscricaoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EventoService {

    private final EventoRepository eventoRepository;
    private final InscricaoRepository inscricaoRepository;

    public EventoService(EventoRepository eventoRepository, InscricaoRepository inscricaoRepository) {
        this.eventoRepository = eventoRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listar() {
        return eventoRepository.findAll()
            .stream()
            .map(this::converter)
            .toList();
    }

    @Transactional(readOnly = true)
    public EventoResponse buscar(Long id) {

        return converter(localizar(id));
    }

    public EventoResponse cadastrar(EventoRequest dados) {
        Evento evento = new Evento(
            null,
            dados.titulo(),
            dados.descricao(),
            dados.data(),
            dados.local(),
            dados.capacidade()
        );

        return converter(eventoRepository.save(evento));
    }

    public EventoResponse atualizar(Long id, EventoRequest dados) {
        Evento evento = localizar(id);
        long inscritos = inscricaoRepository.countByEventoId(id);

        if (dados.capacidade() < inscritos) {
            throw new RegraDeNegocioException(
                "A capacidade não pode ser menor que o número de inscritos"
            );
        }

        evento.setTitulo(dados.titulo());
        evento.setDescricao(dados.descricao());
        evento.setData(dados.data());
        evento.setLocal(dados.local());
        evento.setCapacidade(dados.capacidade());

        return converter(eventoRepository.save(evento));
    }

    public void excluir(Long id) {
        Evento evento = localizar(id);

        if (inscricaoRepository.existsByEventoId(id)) {
            throw new RegraDeNegocioException(
                "Exclua as inscrições antes de excluir este evento"
            );
        }

        eventoRepository.delete(evento);
    }

    public Evento localizar(Long id) {
        return eventoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    private EventoResponse converter(Evento evento) {
        long inscritos = inscricaoRepository.countByEventoId(evento.getId());
        long vagasDisponiveis = evento.getCapacidade() - inscritos;

        return new EventoResponse(
            evento.getId(),
            evento.getTitulo(),
            evento.getDescricao(),
            evento.getData(),
            evento.getLocal(),
            evento.getCapacidade(),
            inscritos,
            vagasDisponiveis
        );
    }
}
