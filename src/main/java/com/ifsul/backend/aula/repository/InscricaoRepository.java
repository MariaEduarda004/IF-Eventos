package com.ifsul.backend.aula.repository;

import com.ifsul.backend.aula.entity.Inscricao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {

    boolean existsByUsuarioIdAndEventoId(Long usuarioId, Long eventoId);

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByEventoId(Long eventoId);

    long countByEventoId(Long eventoId);

    List<Inscricao> findByEventoIdOrderByIdAsc(Long eventoId);
}
