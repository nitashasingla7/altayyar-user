package com.altayyar.users.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Represents invalid code exception i.e. exception thrown when given user verification code is invalid.
 * 
 * @author nitasha
 *
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class InvalidCodeException extends RuntimeException
{
    
    public InvalidCodeException()
    {
    }
    
    public InvalidCodeException(String exception)
    {
        super(exception);
    }

}
