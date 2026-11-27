package com.example.aiservice.adapters.in.web;

import com.example.aiservice.application.CoreIndisponivelException;
import com.example.aiservice.application.IaIndisponivelException;
import com.example.aiservice.application.NaoEncontradaException;
import com.example.aiservice.domain.AcaoJaDecididaException;
import com.example.aiservice.domain.AcessoNegadoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Todos os erros no formato unico do plano (secao 5.4): {"erro": "mensagem legivel"}. */
@RestControllerAdvice
class ErroHandler {

    private static final Logger log = LoggerFactory.getLogger(ErroHandler.class);

    record ErroResponse(String erro) {
    }

    @ExceptionHandler(IaIndisponivelException.class)
    ResponseEntity<ErroResponse> iaIndisponivel(IaIndisponivelException e) {
        return erro(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    ResponseEntity<ErroResponse> acessoNegado(AcessoNegadoException e) {
        return erro(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(NaoEncontradaException.class)
    ResponseEntity<ErroResponse> naoEncontrada(NaoEncontradaException e) {
        return erro(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(AcaoJaDecididaException.class)
    ResponseEntity<ErroResponse> jaDecidida(AcaoJaDecididaException e) {
        return erro(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(CoreIndisponivelException.class)
    ResponseEntity<ErroResponse> coreIndisponivel(CoreIndisponivelException e) {
        return erro(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    @ExceptionHandler(IdentidadeHeaders.IdentidadeAusenteException.class)
    ResponseEntity<ErroResponse> semIdentidade(IdentidadeHeaders.IdentidadeAusenteException e) {
        return erro(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroResponse> invalido(MethodArgumentNotValidException e) {
        String mensagem = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getDefaultMessage())
                .orElse("Requisição inválida.");
        return erro(HttpStatus.BAD_REQUEST, mensagem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException e) {
        return erro(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido. Envie {\"objetivo\": \"...\"}.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErroResponse> argumentoInvalido(IllegalArgumentException e) {
        return erro(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErroResponse> inesperado(Exception e) {
        log.error("Erro inesperado no ai-service", e);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no ai-service.");
    }

    private static ResponseEntity<ErroResponse> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResponse(mensagem));
    }
}
