
package com.aureva.transcribe.service;

import com.aureva.transcribe.application.port.out.TranscriptionEngine;
import com.aureva.transcribe.application.service.TranscriptionEngineRegistry;
import com.aureva.transcribe.domain.model.AudioInput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class AudioTranscriptionService {

    private static final int SAMPLE_RATE = 16_000;
    private static final int CHANNELS = 1;

    private static final long CONVERSION_TIMEOUT_MINUTES = 2;

    // Ограничиваем размер промежуточного WAV-файла.
    private static final long MAX_CONVERTED_AUDIO_BYTES = 9_700_000;

    private final TranscriptionEngineRegistry engineRegistry;
    private final String ffmpegExecutable;
    private final String defaultEngine;

    public AudioTranscriptionService(
            TranscriptionEngineRegistry engineRegistry,
            @Value("${aureva.audio.ffmpeg.executable:ffmpeg}")
            String ffmpegExecutable,
            @Value("${aureva.transcription.default-engine:vosk}")
            String defaultEngine
    ) {
        if (ffmpegExecutable == null || ffmpegExecutable.isBlank()) {
            throw new IllegalArgumentException(
                    "FFmpeg executable must not be empty."
            );
        }

        this.engineRegistry = engineRegistry;
        this.ffmpegExecutable = ffmpegExecutable;
        this.defaultEngine = defaultEngine;
    }

    public String transcribe(
            MultipartFile file,
            String language
    ) throws IOException, InterruptedException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Audio file is empty.");
        }

        if (language == null || language.isBlank()) {
            throw new IllegalArgumentException("Language must not be empty.");
        }

        String normalizedLanguage = language.trim().toLowerCase(Locale.ROOT);

        TranscriptionEngine engine =
                engineRegistry.getEngine(defaultEngine);

        Path workDir = createSecureWorkDirectory();

        Process process = null;
        Throwable failure = null;

        try {
            Path input = workDir.resolve("input.media");
            Path wav = workDir.resolve("audio.wav");

            file.transferTo(input);

            process = new ProcessBuilder(
                    ffmpegExecutable,
                    "-nostdin",
                    "-hide_banner",
                    "-loglevel", "error",
                    "-y",
                    "-i", input.toAbsolutePath().toString(),
                    "-map", "0:a:0",
                    "-vn",
                    "-ac", String.valueOf(CHANNELS),
                    "-ar", String.valueOf(SAMPLE_RATE),
                    "-c:a", "pcm_s16le",
                    "-fs", String.valueOf(MAX_CONVERTED_AUDIO_BYTES),
                    "-f", "wav",
                    wav.toAbsolutePath().toString()
            )
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();

            boolean completed = process.waitFor(
                    CONVERSION_TIMEOUT_MINUTES,
                    TimeUnit.MINUTES
            );

            if (!completed) {
                terminateProcess(process);

                throw new IOException(
                        "Audio conversion exceeded the time limit."
                );
            }

            // ffmpeg stops writing at the size limit, so a file that reached
            // it means the recording is longer than the supported duration.
            if (Files.isRegularFile(wav)
                    && Files.size(wav) >= MAX_CONVERTED_AUDIO_BYTES) {

                throw new AudioTooLongException(
                        "Audio duration exceeds the 5-minute limit."
                );
            }

            if (process.exitValue() != 0
                    || !Files.isRegularFile(wav)
                    || Files.size(wav) <= 44) {

                throw new IOException("Audio conversion failed.");
            }

            AudioInput audioInput = new AudioInput(
                    wav,
                    SAMPLE_RATE,
                    CHANNELS
            );

            return engine.transcribe(audioInput, normalizedLanguage);

        } catch (IOException | InterruptedException | RuntimeException exception) {
            failure = exception;

            if (process != null && process.isAlive()) {
                terminateProcess(process);
            }

            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            throw exception;

        } finally {
            try {
                deleteWorkDirectory(workDir);
            } catch (IOException cleanupException) {
                if (failure != null) {
                    failure.addSuppressed(cleanupException);
                } else {
                    throw cleanupException;
                }
            }
        }
    }

    private void terminateProcess(Process process) {
        if (!process.isAlive()) {
            return;
        }

        process.destroyForcibly();

        boolean interrupted = false;

        // Ждём фактического завершения процесса перед удалением файлов.
        while (process.isAlive()) {
            try {
                process.waitFor();
            } catch (InterruptedException exception) {
                interrupted = true;
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private Path createSecureWorkDirectory() throws IOException {
        Path tempRoot = Path.of(
                System.getProperty("java.io.tmpdir")
        ).toAbsolutePath().normalize();

        FileStore fileStore = Files.getFileStore(tempRoot);

        if (fileStore.supportsFileAttributeView("posix")) {
            FileAttribute<Set<java.nio.file.attribute.PosixFilePermission>>
                    permissions = PosixFilePermissions.asFileAttribute(
                    PosixFilePermissions.fromString("rwx------")
            );

            return Files.createTempDirectory(
                    tempRoot,
                    "aureva-",
                    permissions
            );
        }

        // Windows: используются ACL временной директории.
        return Files.createTempDirectory(tempRoot, "aureva-");
    }

    private void deleteWorkDirectory(Path workDir) throws IOException {
        if (!Files.exists(workDir)) {
            return;
        }

        try (var paths = Files.walk(workDir)) {
            for (Path path : paths
                    .sorted(Comparator.reverseOrder())
                    .toList()) {

                Files.deleteIfExists(path);
            }
        }
    }
}
