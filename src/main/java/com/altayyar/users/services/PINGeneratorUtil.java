package com.altayyar.users.services;

/**
 * Utility class to generate 8 digit random alpha numeric PIN.
 * 
 * @author nitasha
 *
 */
public class PINGeneratorUtil
{

    private static final int ALPHA_NUMERIC_STRING_COUNT = 8; // Alpha numeric string count
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * This method generates a random alphanumeric string of given count.
     * 
     * @return alpha numeric string.
     */
    public static String randomAlphaNumeric()
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
