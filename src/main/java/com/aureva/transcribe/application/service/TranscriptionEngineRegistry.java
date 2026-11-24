package com.aureva.transcribe.application.service;

import com.aureva.transcribe.application.port.out.TranscriptionEngine;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class TranscriptionEngineRegistry {

    private final Map<String, TranscriptionEngine> engines;

    public TranscriptionEngineRegistry(
            List<TranscriptionEngine> availableEngines
    ) {
        Map<String, TranscriptionEngine> engineMap = new HashMap<>();

        for (TranscriptionEngine engine : availableEngines) {
            String engineName = engine.getName();

            if (engineName == null || engineName.isBlank()) {
                throw new IllegalStateException(
                        "Transcription engine name must not be empty."
                );
            }

            engineName = engineName.trim().toLowerCase(Locale.ROOT);

            if (engineMap.putIfAbsent(engineName, engine) != null) {
                throw new IllegalStateException(
                        "Duplicate transcription engine: " + engineName
                );
            }
        }

        this.engines = Map.copyOf(engineMap);
    }

    public TranscriptionEngine getEngine(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Engine name must not be empty."
            );
        }

        String normalizedName = name.trim().toLowerCase(Locale.ROOT);

        TranscriptionEngine engine = engines.get(normalizedName);

        if (engine == null) {
            throw new IllegalArgumentException(
                    "Unknown transcription engine: " + name
            );
        }

        return engine;
    }
}
