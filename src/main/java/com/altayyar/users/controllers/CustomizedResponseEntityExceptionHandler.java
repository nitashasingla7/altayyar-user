package com.altayyar.users.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Customized exception handler. Currently it adds customized/generic error response for all exception types which are
 * not handled.
 * 
 * @author nitasha
 *
 */
@ControllerAdvice
@RestController
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler
{

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Response> handleAllExceptions(Exception ex, WebRequest request)
    {
        Response errorDetails = new Response("error", 0);
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
