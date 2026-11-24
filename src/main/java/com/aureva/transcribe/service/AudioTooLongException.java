package com.aureva.transcribe.service;

/**
 * Thrown when the converted audio exceeds the maximum supported duration.
 */
public class AudioTooLongException extends RuntimeException {

    public AudioTooLongException(String message) {
        super(message);
    }
}
