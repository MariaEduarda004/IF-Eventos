package com.ifsul.backend.aula.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(Long id) {
        super("Registro com ID " + id + " não encontrado");
    }
}
