package com.altayyar.users.controllers;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Response is the response entity type for Verification code APIs. 
 * 
 * @author nitasha
 */
@XmlRootElement(name = "response")
@JsonInclude(Include.NON_NULL)
@JsonAutoDetect(getterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY, isGetterVisibility = Visibility.NONE)
public class Response
{
    // response message e.g. successful/error
    private String message;

    // result code for response
    private Integer resultCode;

    public Response()
    {
        // default constructor
    }

    /**
     * Constructor to create Response entity from given message and result code.
     * 
     * @param message
     * @param resultCode
     */
    public Response(String message, Integer resultCode)
    {
        this.resultCode = resultCode;
        this.message = message;

    }

    /**
     * Returns the response message.
     * 
     * @return message
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * Sets the response message.
     * 
     * @param message sets the message
     */
    public void setMessage(String message)
    {
        this.message = message;
    }

    /**
     * Returns the result code value.
     * 
     * @return result code
     */
    public Integer getResultCode()
    {
        return resultCode;
    }

    /**
     * Sets the result code.
     * 
     * @param resultCode to be set
     */
    public void setResultCode(Integer resultCode)
    {
        this.resultCode = resultCode;
    }

}
