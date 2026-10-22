package com.ifsul.backend.aula.service;

import com.ifsul.backend.aula.dto.InscricaoRequest;
import com.ifsul.backend.aula.dto.InscricaoResponse;
import com.ifsul.backend.aula.entity.Evento;
import com.ifsul.backend.aula.entity.Inscricao;
import com.ifsul.backend.aula.entity.Usuario;
import com.ifsul.backend.aula.exception.RecursoNaoEncontradoException;
import com.ifsul.backend.aula.exception.RegraDeNegocioException;
import com.ifsul.backend.aula.repository.InscricaoRepository;
import com.ifsul.backend.aula.repository.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InscricaoService {

    private final InscricaoRepository inscricaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EventoService eventoService;

    public InscricaoService(InscricaoRepository inscricaoRepository, UsuarioRepository usuarioRepository, EventoService eventoService){
        this.inscricaoRepository = inscricaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.eventoService = eventoService;
    }

    @Transactional(readOnly = true)
    public List<InscricaoResponse> listarPorEvento(Long eventoId) {
        eventoService.localizar(eventoId);

        return inscricaoRepository.findByEventoIdOrderByIdAsc(eventoId)
            .stream()
            .map(this::converter)
            .toList();
    }

    public InscricaoResponse cadastrar(InscricaoRequest dados) {
        Usuario usuario = usuarioRepository.findById(dados.usuarioId())
            .orElseThrow(() -> new RecursoNaoEncontradoException(dados.usuarioId()));

        Evento evento = eventoService.localizar(dados.eventoId());

        boolean jaInscrito = inscricaoRepository.existsByUsuarioIdAndEventoId(
            usuario.getId(), evento.getId()
        );

        if (jaInscrito) {
            throw new RegraDeNegocioException("Usuário já inscrito neste evento");
        }

        long quantidadeInscritos = inscricaoRepository.countByEventoId(evento.getId());

        if (quantidadeInscritos >= evento.getCapacidade()) {
            throw new RegraDeNegocioException("Não há vagas disponíveis neste evento");
        }

        Inscricao novaInscricao = new Inscricao(usuario, evento);
        return converter(inscricaoRepository.save(novaInscricao));
    }

    public void cancelar(Long id) {
        Inscricao inscricao = inscricaoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(id));

        inscricaoRepository.delete(inscricao);
    }

    private InscricaoResponse converter(Inscricao inscricao) {
        return new InscricaoResponse(
            inscricao.getId(),
            inscricao.getUsuario().getId(),
            inscricao.getUsuario().getNome(),
            inscricao.getEvento().getId(),
            inscricao.getEvento().getTitulo(),
            inscricao.getDataInscricao()
        );
    }
}
