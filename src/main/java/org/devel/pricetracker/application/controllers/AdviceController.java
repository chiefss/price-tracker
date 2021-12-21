package org.devel.pricetracker.application.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javassist.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.devel.pricetracker.application.services.MailService;
import org.devel.pricetracker.application.utils.Defines;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;

@Log4j2
@ControllerAdvice
public class AdviceController {

    private final MailService mailService;

    @Autowired
    public AdviceController(MailService mailService) {
        this.mailService = mailService;
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    public String commence(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        // 403
        log.warn(String.format("Access to resource is forbidden (403) for request: \"%s\" \"%s\" error message: \"%s\"",
                request.getMethod(), request.getRequestURL(), accessDeniedException.getMessage()));

        mailService.sendAdmin(String.format("[%s] Access to resource is forbidden (403) for request", request.getServerName()),
                String.format("[%s]\n\nAccess to resource is forbidden (403) for request: \"%s\" \"%s?%s\"" +
                                "\n\nerror message: \"%s\"\n\nTrace:\n\n%s",
                        request.getServerName(), request.getMethod(), request.getRequestURL(), request.getQueryString() != null ? request.getQueryString() : "",
                        accessDeniedException.getMessage(), Arrays.toString(accessDeniedException.getStackTrace())));

        String requestUri = request.getRequestURI();
        if (requestUri.startsWith(Defines.API_PREFIX)) {
            setResponseStatus(response, HttpServletResponse.SC_FORBIDDEN);
            return Defines.API_ERROR_TEMPLATE_NAME;
        } else {
            setResponseStatus(response, HttpServletResponse.SC_NOT_FOUND);
            return Defines.WEB_ERROR_TEMPLATE_NAME;
        }
    }

    @ExceptionHandler(value = {NotFoundException.class})
    public String commence(HttpServletRequest request, HttpServletResponse response, NotFoundException notFoundException) {
        // 404
        log.debug(String.format("Object was not found (404) for request: \"%s\" \"%s\" error message: \"%s\"",
                request.getMethod(), request.getRequestURL(), notFoundException.getMessage()));

        String requestUri = request.getRequestURI();
        if (requestUri.startsWith(Defines.API_PREFIX)) {
            setResponseStatus(response, HttpServletResponse.SC_NOT_FOUND);
            return Defines.API_ERROR_TEMPLATE_NAME;
        } else {
            setResponseStatus(response, HttpServletResponse.SC_NOT_FOUND);
            return Defines.WEB_ERROR_TEMPLATE_NAME;
        }
    }

    @ExceptionHandler(value = {Exception.class})
    public String commence(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        // 500
        log.error(String.format("An error (500) occurred during request: \"%s\" \"%s\" error message: \"%s\"",
                request.getMethod(), request.getRequestURL(), exception.getMessage()));

        mailService.sendAdmin(String.format("[%s] An error (500) occurred during request", request.getServerName()),
                String.format("[%s]\n\nAn error (500) occurred during request: \"%s\" \"%s?%s\"" +
                                "\n\nerror message: \"%s\"\n\nTrace:\n\n%s",
                        request.getServerName(), request.getMethod(), request.getRequestURL(), request.getQueryString() != null ? request.getQueryString() : "",
                        exception.getMessage(), Arrays.toString(exception.getStackTrace())));

        String requestUri = request.getRequestURI();
        if (requestUri.startsWith(Defines.API_PREFIX)) {
            setResponseStatus(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return Defines.API_ERROR_TEMPLATE_NAME;
        } else {
            setResponseStatus(response, HttpServletResponse.SC_NOT_FOUND);
            return Defines.WEB_ERROR_TEMPLATE_NAME;
        }
    }

    private void setResponseStatus(HttpServletResponse response, int status){
        response.setStatus(status);
    }
}
