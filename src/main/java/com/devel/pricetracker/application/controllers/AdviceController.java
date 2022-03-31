package com.devel.pricetracker.application.controllers;

import com.devel.pricetracker.application.services.MailService;
import com.devel.pricetracker.application.utils.Constants;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@ControllerAdvice
public class AdviceController {

    @Autowired
    public AdviceController(MailService mailService) {
        this.mailService = mailService;
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    public String commence(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        // 403
        logger.warn(String.format("Access to resource is forbidden (403) for request: \"%s\" \"%s\" error message: \"%s\"",
                request.getMethod(), request.getRequestURL(), accessDeniedException.getMessage()));

        mailService.sendAdmin(String.format("[%s] Access to resource is forbidden (403) for request", request.getServerName()),
                String.format("[%s]\n\nAccess to resource is forbidden (403) for request: \"%s\" \"%s?%s\"" +
                                "\n\nerror message: \"%s\"\n\nTrace:\n\n%s",
                        request.getServerName(), request.getMethod(), request.getRequestURL(), request.getQueryString() != null ? request.getQueryString() : "",
                        accessDeniedException.getMessage(), Arrays.toString(accessDeniedException.getStackTrace())));

        String requestUri = request.getRequestURI();
        if (requestUri.startsWith(apiPrefix)) {
            setResponseStatus(response, HttpServletResponse.SC_FORBIDDEN);
            return Constants.API_ERROR_TEMPLATE_NAME;
        } else {
            setResponseStatus(response, HttpServletResponse.SC_NOT_FOUND);
            return Constants.WEB_ERROR_TEMPLATE_NAME;
        }
    }

    @ExceptionHandler(value = {NotFoundException.class})
    public String commence(HttpServletRequest request, HttpServletResponse response, NotFoundException notFoundException) {
        // 404
        logger.debug(String.format("Object was not found (404) for request: \"%s\" \"%s\" error message: \"%s\"",
                request.getMethod(), request.getRequestURL(), notFoundException.getMessage()));

        String requestUri = request.getRequestURI();
        if (requestUri.startsWith(apiPrefix)) {
            setResponseStatus(response, HttpServletResponse.SC_NOT_FOUND);
            return Constants.API_ERROR_TEMPLATE_NAME;
        } else {
            setResponseStatus(response, HttpServletResponse.SC_NOT_FOUND);
            return Constants.WEB_ERROR_TEMPLATE_NAME;
        }
    }

    @ExceptionHandler(value = {Exception.class})
    public String commence(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        // 500
        logger.error(String.format("An error (500) occurred during request: \"%s\" \"%s\" error message: \"%s\"",
                request.getMethod(), request.getRequestURL(), exception.getMessage()));

        mailService.sendAdmin(String.format("[%s] An error (500) occurred during request", request.getServerName()),
                String.format("[%s]\n\nAn error (500) occurred during request: \"%s\" \"%s?%s\"" +
                                "\n\nerror message: \"%s\"\n\nTrace:\n\n%s",
                        request.getServerName(), request.getMethod(), request.getRequestURL(), request.getQueryString() != null ? request.getQueryString() : "",
                        exception.getMessage(), Arrays.toString(exception.getStackTrace())));

        String requestUri = request.getRequestURI();
        if (requestUri.startsWith(apiPrefix)) {
            setResponseStatus(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return Constants.API_ERROR_TEMPLATE_NAME;
        } else {
            setResponseStatus(response, HttpServletResponse.SC_NOT_FOUND);
            return Constants.WEB_ERROR_TEMPLATE_NAME;
        }
    }

    private void setResponseStatus(HttpServletResponse response, int status){
        response.setStatus(status);
    }

    private final String apiPrefix = "/api/";

    private final MailService mailService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
}
