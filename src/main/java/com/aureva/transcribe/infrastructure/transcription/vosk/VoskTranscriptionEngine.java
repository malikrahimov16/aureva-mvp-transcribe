package com.aureva.transcribe.infrastructure.transcription.vosk;

import com.aureva.transcribe.application.port.out.TranscriptionEngine;
import com.aureva.transcribe.domain.model.AudioInput;
import com.aureva.transcribe.service.TranscriptionService;
import org.springframework.stereotype.Component;
import org.vosk.Model;
import org.vosk.Recognizer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;

@Component
public class VoskTranscriptionEngine implements TranscriptionEngine {

    private static final String ENGINE_NAME = "vosk";

    private final TranscriptionService transcriptionService;
    private final ObjectMapper objectMapper;

    public VoskTranscriptionEngine(
            TranscriptionService transcriptionService,
            ObjectMapper objectMapper
    ) {
        this.transcriptionService = transcriptionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return ENGINE_NAME;
    }

    @Override
    public String transcribe(AudioInput audioInput, String language) {
        if (audioInput.channels() != 1) {
            throw new IllegalArgumentException(
                    "Vosk requires mono audio."
            );
        }

        Model model = getModel(language);

        try (AudioInputStream audioStream =
                     AudioSystem.getAudioInputStream(audioInput.path().toFile());
             Recognizer recognizer =
                     new Recognizer(model, audioInput.sampleRate())) {

            StringBuilder transcript = new StringBuilder();

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = audioStream.read(buffer)) != -1) {
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    appendResult(transcript, recognizer.getResult());
                }
            }

            appendResult(transcript, recognizer.getFinalResult());

            return transcript.toString();

        } catch (UnsupportedAudioFileException exception) {
            throw new IllegalArgumentException(
                    "Unsupported audio format.",
                    exception
            );
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Failed to process audio with Vosk.",
                    exception
            );
        }
    }

    private void appendResult(StringBuilder transcript, String result)
            throws IOException {

        JsonNode jsonNode = objectMapper.readTree(result);
        String recognizedText = jsonNode.path("text").asString("");

        if (!recognizedText.isBlank()) {
            if (!transcript.isEmpty()) {
                transcript.append(' ');
            }

            transcript.append(recognizedText);
        }
    }

    private Model getModel(String language) {
        try {
            return switch (language.toLowerCase(Locale.ROOT)) {
                case "en" -> transcriptionService.getEnglishModel();
                case "ru" -> transcriptionService.getRussianModel();
                default -> throw new IllegalArgumentException(
                        "Unsupported language: " + language
                );
            };
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Failed to load Vosk model.",
                    exception
            );
        }
    }
}
