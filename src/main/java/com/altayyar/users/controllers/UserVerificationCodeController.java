package com.altayyar.users.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.altayyar.users.services.UserVerificationCodeService;

/**
 * This class provides the request mapping methods for the REST API endpoints that expose any User Verification Code
 * related operations.
 *
 * @author nitasha
 */
@RestController
@RequestMapping("/verification_code")
public class UserVerificationCodeController
{

    @Autowired
    private UserVerificationCodeService userVerificationCodeService;

    /**
     * Generates verification code for user with User Id. 
     * Example URI - POST /verification_code/test123.json
     * response :    {
     *                  "message":"successful",
     *                  "resultCode":1
     *               }
     * 
     * @param userId - user id
     * @return {@ink Response} entity containing message and result code.
     */
    @RequestMapping(value = "/{user_id}", method = RequestMethod.POST, produces = { "application/json",
            "application/xml" })
    public Response generateUserVerificationCode(@PathVariable("user_id") String userId)
    {
        userVerificationCodeService.createAndSaveVerificationCode(userId);

        return successResponse();
    }

    /**
     * Verifies the given user code for user id.
     *  
     * Example URI - GET /verification_code/test123/ABG789HX.json
     * response :    {
     *                  "valid":"true",
     *                  "resultCode":1
     *               }
     * 
     * @param userId - user id
     * @param verificationCode - verification code
     * @return
     */
    @RequestMapping(value = "/{user_id}/{code}", method = RequestMethod.GET, produces = { "application/json",
            "application/xml", "text/csv" })
    public Response verifyUserCode(@PathVariable("user_id") String userId,
            @PathVariable("code") String verificationCode)
    {
        if (!userVerificationCodeService.verifyUserCode(userId, verificationCode))
            throw new InvalidCodeException();

        return verifyCodeResponse(1, true);
    }

    /**
     * Handles response when verification code is invalid .
     * 
     * @return invalid code response.
     */
    @ExceptionHandler(InvalidCodeException.class)
    public Response handleVerifyCodeError()
    {
        return verifyCodeResponse(1, false);
    }

    /**
     * Creates a Response entity with successful response.
     * 
     * @return response entity
     */
    private Response successResponse()
    {
        return new Response("successful", 1);
    }

    /**
     * Creates verify code response entity with given result code and valid flag.
     * 
     * @param resultCode - result code
     * @param isValid - valid flag
     * @return {@link VerifyCodeResponse}
     */
    private VerifyCodeResponse verifyCodeResponse(int resultCode, Boolean isValid)
    {
        VerifyCodeResponse response = new VerifyCodeResponse();
        response.setValid(isValid);
        response.setResultCode(resultCode);
        return response;
    }
}
