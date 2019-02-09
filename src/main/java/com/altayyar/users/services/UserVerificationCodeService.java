package com.altayyar.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.altayyar.users.model.User;
import com.altayyar.users.repository.UserRepository;

/**
 * User Verification code service to handle user verification code generation and verification etc operations.
 * 
 * @author nitasha
 *
 */
@Component
public class UserVerificationCodeService
{

    @Autowired
    private UserRepository userRepository;

    /**
     * This method finds an existing user with given userId Or creates new user if there is no existing user with given
     * id. Generates an eight digit alphanumeric random PIN and saves it to database.
     * 
     * @param userId
     */
    public void createAndSaveVerificationCode(String userId)
    {
        Assert.notNull(userId, "User Id should not be null.");

        User user = findOrCreateUser(userId);

        String verficationCode = PINGeneratorUtil.randomAlphaNumeric();
        user.setVerficationCode(verficationCode);

        userRepository.save(user);
    }

    /**
     * This method find an existing user with given user id and verifies stored code for user with given verification
     * code.
     * 
     * @param userId - user if
     * @param verificationCode - code to be verified
     * @return true|false based on whether the code is correct or not
     */
    public boolean verifyUserCode(String userId, String verificationCode)
    {
        Assert.notNull(userId, "User Id should not be null.");
        Assert.notNull(verificationCode, "Verification code should not be null.");

        User user = userRepository.findByUserId(userId);

        return user != null ? user.getVerficationCode().equals(verificationCode) : false;
    }

    private User findOrCreateUser(String userId)
    {
        User user = userRepository.findByUserId(userId);

        if (user == null)
        {
            user = new User();
            user.setUserId(userId);
        }
        return user;
    }
}
