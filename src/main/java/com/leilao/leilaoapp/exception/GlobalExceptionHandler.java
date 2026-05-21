package com.leilao.leilaoapp.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Recursos estáticos não encontrados (favicon.ico, CSS, JS, etc.)
     * Retorna 404 silenciosamente, sem logar erro.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNoResource(NoResourceFoundException ex) {
        // Silencioso — browser pede favicon.ico, robots.txt, etc.
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoHandlerFoundException ex, Model model) {
        model.addAttribute("mensagem", "A página que você buscou não foi encontrada.");
        return "error/404";
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatus(ResponseStatusException ex, Model model) {
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            model.addAttribute("mensagem", ex.getReason() != null
                    ? ex.getReason() : "Recurso não encontrado.");
            return "error/404";
        }
        log.error("Erro HTTP {}: {}", ex.getStatusCode(), ex.getReason());
        model.addAttribute("mensagem", "Ocorreu um erro inesperado. Tente novamente.");
        return "error/500";
    }

    @ExceptionHandler(LancesException.class)
    public String handleLancesException(LancesException ex, Model model) {
        model.addAttribute("mensagem", ex.getMessage());
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception ex, Model model) {
        log.error("Erro não tratado: {}", ex.getMessage(), ex);
        model.addAttribute("mensagem", "Ocorreu um erro inesperado. Tente novamente mais tarde.");
        return "error/500";
    }
}
