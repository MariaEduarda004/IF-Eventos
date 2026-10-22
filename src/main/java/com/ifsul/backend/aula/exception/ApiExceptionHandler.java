package com.ifsul.backend.aula.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(RecursoNaoEncontradoException erro) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("erro", erro.getMessage()));
    }

    @ExceptionHandler({EmailDuplicadoException.class, RegraDeNegocioException.class})
    public ResponseEntity<Map<String, String>> conflito(RuntimeException erro) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("erro", erro.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> dadosInvalidos(MethodArgumentNotValidException erro) {
        String mensagem = erro.getBindingResult()
            .getFieldErrors()
            .stream()
            .findFirst()
            .map(campo -> campo.getDefaultMessage())
            .orElse("Dados inválidos");

        return ResponseEntity.badRequest()
            .body(Map.of("erro", mensagem));
    }
}
