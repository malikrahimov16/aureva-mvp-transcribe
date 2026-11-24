package com.aureva.transcribe.tests.utils;

import java.util.regex.Pattern;

/**
 * Checks that a transcript is written in the expected language.
 * Catches the wrong model or a wrong language being used,
 * without depending on the exact recognised words.
 */
public final class TranscriptChecks {

    private static final Pattern CYRILLIC = Pattern.compile("[\\u0400-\\u04FF]");
    private static final Pattern LATIN = Pattern.compile("[A-Za-z]");

    private TranscriptChecks() {
    }

    public static boolean isEnglish(String text) {
        return text != null
                && LATIN.matcher(text).find()
                && !CYRILLIC.matcher(text).find();
    }

    public static boolean isRussian(String text) {
        return text != null
                && CYRILLIC.matcher(text).find()
                && !LATIN.matcher(text).find();
    }
}
