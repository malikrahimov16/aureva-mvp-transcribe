package com.aureva.transcribe.controller;

import com.aureva.transcribe.domain.model.AudioTranscription;
import com.aureva.transcribe.repository.AudioTranscriptionRepository;
import com.aureva.transcribe.service.AudioTooLongException;
import com.aureva.transcribe.service.AudioTranscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private static final Logger log =
            LoggerFactory.getLogger(UploadController.class);

    private static final String ERROR_KEY = "error";

    private static final long MAX_FILE_SIZE_BYTES =
            50L * 1024 * 1024;

    private static final int MAX_FILENAME_LENGTH = 255;

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("mp3", "wav", "m4a", "mp4", "webm", "weba");

    private static final Set<String> ALLOWED_LANGUAGES =
            Set.of("en", "ru");

    private final AudioTranscriptionService audioTranscriptionService;
    private final AudioTranscriptionRepository audioTranscriptionRepository;

    public UploadController(
            AudioTranscriptionService audioTranscriptionService,
            AudioTranscriptionRepository audioTranscriptionRepository
    ) {
        this.audioTranscriptionService = audioTranscriptionService;
        this.audioTranscriptionRepository = audioTranscriptionRepository;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(defaultValue = "en") String language
    ) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(ERROR_KEY, "Please select a file."));
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
                    .body(Map.of(ERROR_KEY, "File exceeds the 50 MiB limit."));
        }

        String originalFilename = sanitizeFilename(
                file.getOriginalFilename()
        );

        if (originalFilename.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(ERROR_KEY, "File name is missing."));
        }

        if (originalFilename.length() > MAX_FILENAME_LENGTH) {
            return ResponseEntity.badRequest()
                    .body(Map.of(ERROR_KEY, "File name is too long."));
        }

        String extension = extractExtension(originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return ResponseEntity.badRequest()
                    .body(Map.of(ERROR_KEY, "Unsupported file format."));
        }

        if (language == null || language.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(ERROR_KEY, "Language must not be empty."));
        }

        String normalizedLanguage = language.trim().toLowerCase(Locale.ROOT);

        if (!ALLOWED_LANGUAGES.contains(normalizedLanguage)) {
            return ResponseEntity.badRequest()
                    .body(Map.of(ERROR_KEY, "Unsupported language."));
        }

        try {
            String transcript = audioTranscriptionService.transcribe(
                    file,
                    normalizedLanguage
            );

            AudioTranscription audioTranscription =
                    new AudioTranscription(
                            originalFilename,
                            normalizedLanguage,
                            transcript
                    );

            audioTranscriptionRepository.save(audioTranscription);

            return ResponseEntity.ok(Map.of(
                    "message", "Transcription completed.",
                    "fileName", originalFilename,
                    "sizeBytes", String.valueOf(file.getSize()),
                    "extension", extension,
                    "language", normalizedLanguage,
                    "transcript", transcript
            ));

        } catch (AudioTooLongException exception) {
            log.warn(
                    "Audio too long for file '{}', language '{}'.",
                    originalFilename,
                    normalizedLanguage
            );

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            ERROR_KEY,
                            "File duration exceeds the 5-minute limit."
                    ));

        } catch (IllegalArgumentException exception) {
            log.warn(
                    "Invalid upload parameters for file '{}', language '{}'.",
                    originalFilename,
                    normalizedLanguage,
                    exception
            );

            return ResponseEntity.badRequest()
                    .body(Map.of(ERROR_KEY, "Invalid upload parameters."));

        } catch (UncheckedIOException exception) {
            log.error(
                    "Unchecked I/O error while processing file '{}', language '{}'.",
                    originalFilename,
                    normalizedLanguage,
                    exception
            );

            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_CONTENT)
                    .body(Map.of(ERROR_KEY, "Audio processing failed."));

        } catch (IOException exception) {
            log.error(
                    "I/O error while processing file '{}', language '{}'.",
                    originalFilename,
                    normalizedLanguage,
                    exception
            );

            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_CONTENT)
                    .body(Map.of(ERROR_KEY, "Audio processing failed."));

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            log.warn(
                    "Audio processing interrupted for file '{}', language '{}'.",
                    originalFilename,
                    normalizedLanguage,
                    exception
            );

            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(ERROR_KEY, "Audio processing was interrupted."));
        }
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }

        String normalized = filename.replace('\\', '/');

        int lastSeparator = normalized.lastIndexOf('/');

        String safeFilename = normalized.substring(lastSeparator + 1);

        safeFilename = safeFilename.replaceAll("\\p{Cntrl}", "");

        return safeFilename;
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');

        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(dotIndex + 1)
                .toLowerCase(Locale.ROOT);
    }
}
