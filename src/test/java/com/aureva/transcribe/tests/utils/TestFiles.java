package com.aureva.transcribe.tests.utils;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Helpers for test audio files. Files are loaded from the classpath,
 * so tests do not depend on the working directory.
 */
public final class TestFiles {

    private TestFiles() {
    }

    public static Path audio(String fileName) {
        URL url = TestFiles.class.getResource("/audio/" + fileName);

        if (url == null) {
            throw new IllegalStateException(
                    "Test audio not found in src/test/resources/audio: " + fileName
            );
        }

        try {
            return Path.of(url.toURI());
        } catch (URISyntaxException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static byte[] audioBytes(String fileName) throws IOException {
        return Files.readAllBytes(audio(fileName));
    }

    /**
     * Creates a WAV file (16 kHz, 16-bit, mono) with silence of the given length.
     */
    public static byte[] silentWav(int durationSeconds) throws IOException {
        AudioFormat format = new AudioFormat(16_000f, 16, 1, true, false);
        byte[] data = new byte[16_000 * 2 * durationSeconds];

        try (AudioInputStream stream = new AudioInputStream(
                new ByteArrayInputStream(data), format, data.length / 2);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, output);

            return output.toByteArray();
        }
    }
}
