package com.altayyar.users.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Model entity class for User.
 * 
 * @author nitasha
 *
 */
@Entity
@Table(name = "USER")
public class User
{
    private static final long serialVersionUID = 1L;

    /**
     * User id - primary key and identification for user.
     */
    @Id
    @Column(name = "USER_ID")
    private String userId;

    /**
     * verification code - user's verification pin
     */
    @Column(name = "VERIFICATION_CODE")
    private String verficationCode;

    /**
     * Returns the user id.
     * 
     * @return userId
     */
    public String getUserId()
    {
        return userId;
    }

    /**
     * Sets the given user id.
     * 
     * @param userId
     */
    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    /**
     * Returns the user's verification code.
     * 
     * @return verification code
     */
    public String getVerficationCode()
    {
        return verficationCode;
    }

    /**
     * Sets the alphanumeric {@link VerificationAfterDelay} code for user.
     * 
     * @param verficationCode
     */
    public void setVerficationCode(String verficationCode)
    {
        this.verficationCode = verficationCode;
    }

}
