package com.ifsul.backend.aula.repository;

import com.ifsul.backend.aula.entity.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, Long> {
}
