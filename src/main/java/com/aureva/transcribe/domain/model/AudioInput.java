package com.aureva.transcribe.domain.model;

import java.nio.file.Path;
import java.util.Objects;

public record AudioInput(
        Path path,
        int sampleRate,
        int channels
) {

    public AudioInput {
        Objects.requireNonNull(path, "Audio path must not be null.");

        path = path.toAbsolutePath().normalize();

        if (sampleRate <= 0) {
            throw new IllegalArgumentException(
                    "Sample rate must be positive."
            );
        }

        if (channels <= 0) {
            throw new IllegalArgumentException(
                    "Number of channels must be positive."
            );
        }
    }
}
