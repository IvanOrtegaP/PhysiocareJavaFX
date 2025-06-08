package com.example.physiocare.utils;

import java.util.regex.Pattern;

/**
 * Utility class for performing common text validations.
 */
public class ValidateUtils {

    /**
     * Verifies that a text is not null or empty after removing whitespace.
     * @param text the text to validate
     * @return {@code true} if the text is not null and not empty after trimming, otherwise {@code false}
     */
    public static boolean validateNotEmpty(String text){
        return text != null && !text.trim().isEmpty();
    }

    /**
     * Validates that the text length is within a specific range.
     * @param text the text to validate
     * @param min minimum allowed length (inclusive)
     * @param max maximum allowed length (inclusive)
     * @return {@code true} if the text is not null and its length (without leading and trailing spaces)
     * is between min and max, otherwise {@code false}
     */
    public static boolean validateLength(String text, int min, int max){

        if(text == null)
            return false;

        int sizeString = text.trim().length();

        return sizeString >= min && sizeString <= max;
    }

    /**
     * Checks if a text matches a provided regular expression.
     * @param text the text to validate
     * @param regex the regular expression pattern against which the text will be evaluated
     * @return {@code true} if the text is not null and matches the pattern, otherwise {@code false}
     */
    public static Boolean validateRegex(String text, Pattern regex){
        return text != null && regex.matcher(text).find();
    }

}
