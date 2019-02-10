package com.altayyar.users.services;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.altayyar.users.model.User;
import com.altayyar.users.repository.UserRepository;

/**
 * Tests the APIs in {@link UserVerificationCodeService}.
 * 
 * @author nitasha
 */
@RunWith(MockitoJUnitRunner.class)
public class UserVerificationCodeServiceTest
{

    @InjectMocks
    private UserVerificationCodeService testService;

    @Mock
    private PINGeneratorUtil pinGeneratorUtil;

    @Mock
    private UserRepository userRepository;

    /**
     * Tests {@link UserVerificationCodeService#createAndSaveVerificationCode(String)} .
     */
    @Test
    public void testUserCodeGeneration()
    {
        String userId = "testUser";
        User user = new User();
        user.setUserId(userId);

        when(userRepository.findByUserId(userId)).thenReturn(user);

        testService.createAndSaveVerificationCode(userId);

        verify(pinGeneratorUtil, times(1)).randomAlphaNumeric();
        verify(userRepository, times(1)).save((user));
    }

    /**
     * Tests {@link UserVerificationCodeService#verifyUserCode(String, String)} when code is valid.
     */
    @Test
    public void testUserCodeVerification_valid()
    {
        String userId = "testUser";
        String code = "BNBH78";
        User user = new User();
        user.setUserId(userId);
        user.setVerficationCode(code);

        when(userRepository.findByUserId(userId)).thenReturn(user);

        boolean valid = testService.verifyUserCode(userId, code);

        assertTrue(valid);
    }

    /**
     * Tests {@link UserVerificationCodeService#verifyUserCode(String, String)} when code is invalid.
     */
    @Test
    public void testUserCodeVerification_invalid()
    {
        String userId = "testUser";
        String code = "BNBH78";
        User user = new User();
        user.setUserId(userId);
        user.setVerficationCode(code);

        when(userRepository.findByUserId(userId)).thenReturn(user);

        boolean valid = testService.verifyUserCode(userId, "XXX");

        assertFalse(valid);
    }

    /**
     * Tests {@link UserVerificationCodeService#createAndSaveVerificationCode(String)} when user is null.
     */
    @Test(expected = Exception.class)
    public void testUserCodeGeneration_WhenUserIdIsNull()
    {
        testService.createAndSaveVerificationCode(null);
    }

    /**
     * Tests {@link UserVerificationCodeService#verifyUserCode(String, String)} when user is null.
     */
    @Test(expected = Exception.class)
    public void testUserCodeVerification_WhenUserIdIsNull()
    {
        testService.verifyUserCode(null, "code");
    }

    /**
     * Tests {@link UserVerificationCodeService#verifyUserCode(String, String)} when code is null.
     */
    @Test(expected = Exception.class)
    public void testUserCodeVerification_WhenCodeIdIsNull()
    {
        testService.verifyUserCode("userId", null);
    }

}
