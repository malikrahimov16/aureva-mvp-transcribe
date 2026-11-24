package com.aureva.transcribe.service;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vosk.Model;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class TranscriptionService {

    private final Path englishModelPath;
    private final Path russianModelPath;

    private Model englishModel;
    private Model russianModel;

    public TranscriptionService(
            @Value("${aureva.transcription.engines.vosk.models.english}")
            String englishModelPath,
            @Value("${aureva.transcription.engines.vosk.models.russian}")
            String russianModelPath
    ) {
        this.englishModelPath = Path.of(englishModelPath)
                .toAbsolutePath()
                .normalize();

        this.russianModelPath = Path.of(russianModelPath)
                .toAbsolutePath()
                .normalize();
    }

    public synchronized Model getEnglishModel() throws IOException {
        if (englishModel == null) {
            englishModel = loadModel(englishModelPath);
        }

        return englishModel;
    }

    public synchronized Model getRussianModel() throws IOException {
        if (russianModel == null) {
            russianModel = loadModel(russianModelPath);
        }

        return russianModel;
    }

    private Model loadModel(Path modelPath) throws IOException {
        if (!Files.isDirectory(modelPath) || !Files.isReadable(modelPath)) {
            throw new IOException(
                    "Vosk model directory does not exist or is not readable."
            );
        }

        return new Model(modelPath.toString());
    }

    @PreDestroy
    public synchronized void closeModels() {
        if (englishModel != null) {
            englishModel.close();
            englishModel = null;
        }

        if (russianModel != null) {
            russianModel.close();
            russianModel = null;
        }
    }
}
