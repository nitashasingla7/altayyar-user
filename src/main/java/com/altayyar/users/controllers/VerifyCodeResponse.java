package com.altayyar.users.controllers;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

/**
 * Verification code response is the response entity type for Verification code API.
 * 
 * @author nitasha
 *
 */
@XmlRootElement(name="response")
@JsonAutoDetect(getterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY, isGetterVisibility = Visibility.NONE)
public class VerifyCodeResponse extends Response
{

    /**
     * Boolean representing if the code is valid or not
     */
    private boolean valid;

    /**
     * Returns true | false based on code is valid or not
     *  
     * @return true | false
     */
    public boolean isValid()
    {
        return valid;
    }

    /**
     * Sets the boolean for code.
     * 
     * @param valid - boolean value to be set 
     */
    public void setValid(boolean valid)
    {
        this.valid = valid;
    }

}
