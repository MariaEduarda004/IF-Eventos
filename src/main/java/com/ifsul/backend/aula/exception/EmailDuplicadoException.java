package com.ifsul.backend.aula.exception;

public class EmailDuplicadoException extends RuntimeException {

    public EmailDuplicadoException(String email) {

        super("O e-mail " + email + " já está cadastrado");
    }
}
