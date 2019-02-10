package com.altayyar.users.services;

import org.springframework.stereotype.Component;

/**
 * Utility class to generate 6 digit random alpha numeric PIN.
 * 
 * @author nitasha
 *
 */
@Component
public class PINGeneratorUtil
{

    private static final int ALPHA_NUMERIC_STRING_COUNT = 6; // Alpha numeric string count
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * This method generates a random alphanumeric string of given count.
     * 
     * @return alpha numeric string.
     */
    public String randomAlphaNumeric()
    {
        int count = ALPHA_NUMERIC_STRING_COUNT;
        StringBuilder builder = new StringBuilder();
        while (count-- != 0)
        {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

}
