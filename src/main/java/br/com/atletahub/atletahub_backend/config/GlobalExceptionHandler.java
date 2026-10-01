package br.com.atletahub.atletahub_backend.config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratamento global de erros. Toda resposta de erro sai no formato {"message": "..."},
 * o mesmo que o login e o filtro de segurança já usam, para o front exibir uma mensagem útil.
 * Nunca devolve stack trace nem detalhe interno (banco, classes) ao cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static ResponseEntity<Map<String, Object>> resposta(HttpStatusCode status, String mensagem) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("message", mensagem);
        return ResponseEntity.status(status).body(corpo);
    }

    // --- Validação de @Valid no corpo (400) ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        String primeira = campos.isEmpty() ? "Dados inválidos." : campos.values().iterator().next();

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("message", primeira);
        corpo.put("errors", campos);
        return ResponseEntity.badRequest().body(corpo);
    }

    // --- JSON malformado, enum inexistente, data inválida... (400) ---
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> corpoIlegivel(HttpMessageNotReadableException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisição inválida. Verifique os dados enviados.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + ex.getName());
    }

    // --- Erros de regra de negócio lançados pelos services ---
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> argumentoInvalido(IllegalArgumentException ex) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage() != null ? ex.getMessage() : "Dados inválidos.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> statusExplicito(ResponseStatusException ex) {
        String motivo = ex.getReason() != null ? ex.getReason() : HttpStatus.valueOf(ex.getStatusCode().value()).getReasonPhrase();
        if (ex.getStatusCode().is5xxServerError()) {
            logger.warn("Erro {} ({})", ex.getStatusCode().value(), motivo);
        }
        return resposta(ex.getStatusCode(), motivo);
    }

    // --- Banco: duplicidade / valor grande demais (409) ---
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integridade(DataIntegrityViolationException ex) {
        logger.warn("Violação de integridade no banco: {}", ex.getMostSpecificCause().getMessage());
        return resposta(HttpStatus.CONFLICT,
                "Não foi possível salvar: o registro já existe ou algum campo excede o tamanho permitido.");
    }

    // --- Upload grande demais (413) ---
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> uploadGrande(MaxUploadSizeExceededException ex) {
        return resposta(HttpStatus.PAYLOAD_TOO_LARGE, "Arquivo muito grande.");
    }

    // --- Segurança: se um AccessDenied/Authentication chegar aqui, mantém 403/401 (e não vira 500) ---
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> acessoNegado(AccessDeniedException ex) {
        return resposta(HttpStatus.FORBIDDEN, "Acesso negado.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> naoAutenticado(AuthenticationException ex) {
        return resposta(HttpStatus.UNAUTHORIZED, "Não autenticado.");
    }

    // --- Rede de segurança: qualquer outra coisa ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> inesperado(Exception ex, HttpServletRequest request) {
        // Exceções padrão do Spring MVC (405, 415, 404 de recurso, parâmetro ausente...) já trazem o status certo.
        if (ex instanceof ErrorResponse erro) {
            HttpStatusCode status = erro.getStatusCode();
            String mensagem = status.is4xxClientError() ? "Requisição inválida." : "Erro ao processar a requisição.";
            return resposta(status, mensagem);
        }
        logger.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado. Tente novamente em instantes.");
    }
}
