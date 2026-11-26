package com.example.grupo_5_gameeducation.comum.erro;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Converte qualquer erro do Core no formato {"erro": "..."}. Assim o front e os outros
 * servicos (ai-service) tratam todos os erros do mesmo jeito.
 */
@RestControllerAdvice
public class ErroHandler {

    private static final Logger log = LoggerFactory.getLogger(ErroHandler.class);

    @ExceptionHandler(NaoEncontradoException.class)
    ResponseEntity<ErroResponse> naoEncontrado(NaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    ResponseEntity<ErroResponse> acessoNegado(AcessoNegadoException ex) {
        return resposta(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    ResponseEntity<ErroResponse> requisicaoInvalida(RequisicaoInvalidaException ex) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    ResponseEntity<ErroResponse> conflito(ConflitoException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ServicoIndisponivelException.class)
    ResponseEntity<ErroResponse> servicoIndisponivel(ServicoIndisponivelException ex) {
        log.warn("Servico indisponivel: {}", ex.getMessage());
        return resposta(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    /** @Valid falhou: junta as mensagens dos campos ("titulo: nao pode ser vazio; ..."). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(campo -> campo.getField() + ": " + campo.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return resposta(HttpStatus.BAD_REQUEST, mensagem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException ex) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Corpo da requisicao invalido. Confira o JSON e os valores dos campos (ex.: area e nivel).");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErroResponse> parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Valor invalido para '" + ex.getName() + "': " + ex.getValue());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErroResponse> parametroAusente(MissingServletRequestParameterException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Parametro obrigatorio ausente: " + ex.getParameterName());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    ResponseEntity<ErroResponse> headerAusente(MissingRequestHeaderException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Header obrigatorio ausente: " + ex.getHeaderName());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErroResponse> rotaInexistente(NoResourceFoundException ex) {
        return resposta(HttpStatus.NOT_FOUND, "Rota nao encontrada: /" + ex.getResourcePath());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErroResponse> metodoNaoSuportado(HttpRequestMethodNotSupportedException ex) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "Metodo " + ex.getMethod() + " nao suportado nesta rota.");
    }

    /** Qualquer outro erro: 500 sem vazar detalhe interno (o detalhe vai para o log). */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErroResponse> inesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde.");
    }

    private static ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResponse(mensagem));
    }
}
