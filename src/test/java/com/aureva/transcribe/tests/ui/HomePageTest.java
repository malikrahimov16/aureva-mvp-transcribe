package com.aureva.transcribe.tests.ui;

import com.aureva.transcribe.tests.base.BaseTest;
import com.aureva.transcribe.tests.config.TestConfig;
import com.aureva.transcribe.tests.pages.HomePage;
import com.aureva.transcribe.tests.utils.TestFiles;
import com.aureva.transcribe.tests.utils.TranscriptChecks;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

public class HomePageTest extends BaseTest {

    private static final String BASE_URL = TestConfig.BASE_URL;

    private static final Path ENGLISH_AUDIO = TestFiles.audio("sample-en.mp3");

    private static final Path RUSSIAN_AUDIO = TestFiles.audio("sample-ru.mp3");

    // Verify that the home page opens

    @Test(description = "Verify home page is displayed")
    public void shouldDisplayHomePage() {
        HomePage homePage = new HomePage(page);
        homePage.open(BASE_URL);

        Assert.assertTrue(homePage.isBrandVisible(),
                "AUREVA brand should be visible");
        Assert.assertTrue(homePage.isUploadTabVisible(),
                "Upload tab should be visible");
    }

    // Verify page title

    @Test(description = "Verify home page title")
    public void shouldDisplayCorrectPageTitle() {
        HomePage homePage = new HomePage(page);
        homePage.open(BASE_URL);

        Assert.assertEquals(page.title(), "AUREVA — AI Transcription",
                "Page title does not match");
    }

    // Verify transcription button

    @Test(description = "Verify transcription button is visible")
    public void shouldDisplayTranscribeButton() {
        HomePage homePage = new HomePage(page);
        homePage.open(BASE_URL);

        Assert.assertTrue(homePage.isTranscribeButtonVisible(),
                "Transcribe button should be visible");
    }

    // Verify English audio file upload

    @Test(description = "Verify English audio file upload")
    public void shouldUploadEnglishAudioSuccessfully() {
        HomePage homePage = new HomePage(page);
        homePage.open(BASE_URL);
        homePage.uploadFile(ENGLISH_AUDIO);

        Assert.assertEquals(homePage.getSelectedFileName(), "sample-en.mp3",
                "English audio file name does not match");
        homePage.waitForTranscribeButtonState(true);
        Assert.assertTrue(homePage.isTranscribeButtonEnabled(),
                "Transcribe button should be enabled after English file upload");
    }

    // Verify Russian audio file upload

    @Test(description = "Verify Russian audio file upload")
    public void shouldUploadRussianAudioSuccessfully() {
        HomePage homePage = new HomePage(page);
        homePage.open(BASE_URL);
        homePage.uploadFile(RUSSIAN_AUDIO);

        Assert.assertEquals(homePage.getSelectedFileName(), "sample-ru.mp3",
                "Russian audio file name does not match");
        homePage.waitForTranscribeButtonState(true);
        Assert.assertTrue(homePage.isTranscribeButtonEnabled(),
                "Transcribe button should be enabled after Russian file upload");
    }

    // Verify English audio transcription

    @Test(description = "Verify English audio transcription", timeOut = 360_000)
    public void shouldTranscribeEnglishAudioSuccessfully() {
        HomePage homePage = new HomePage(page);
        homePage.open(BASE_URL);

        String transcript = homePage.transcribeEnglishAudio(ENGLISH_AUDIO);

        Assert.assertFalse(transcript.isEmpty(),
                "English transcript should not be empty");
        Assert.assertFalse(homePage.getTranscriptText().isEmpty(),
                "English transcript should be displayed");
        Assert.assertTrue(TranscriptChecks.isEnglish(transcript),
                "English transcript should contain Latin letters only");
    }

    // Verify Russian audio transcription

    @Test(description = "Verify Russian audio transcription", timeOut = 360_000)
    public void shouldTranscribeRussianAudioSuccessfully() {
        HomePage homePage = new HomePage(page);
        homePage.open(BASE_URL);

        String transcript = homePage.transcribeRussianAudio(RUSSIAN_AUDIO);

        Assert.assertFalse(transcript.isEmpty(),
                "Russian transcript should not be empty");
        Assert.assertFalse(homePage.getTranscriptText().isEmpty(),
                "Russian transcript should be displayed");
        Assert.assertTrue(TranscriptChecks.isRussian(transcript),
                "Russian transcript should contain Cyrillic letters only");
    }

    // Verify current URL

    @Test(description = "Verify home page URL")
    public void shouldOpenHomePageWithCorrectUrl() {
        HomePage homePage = new HomePage(page);
        homePage.open(BASE_URL);

        Assert.assertEquals(homePage.getCurrentUrl(), BASE_URL + "/#transcribe",
                "Home page URL does not match");
    }

    // Verify default transcription language

    @Test(description = "Verify default transcription language")
    public void shouldSelectEnglishLanguageByDefault() {
        HomePage homePage = new HomePage(page);
        homePage.open(BASE_URL);

        Assert.assertEquals(homePage.getSelectedLanguage(), "en",
                "Default transcription language should be English");
    }

    // Verify switching language to Russian

    @Test(description = "Verify switching language to Russian")
    public void shouldSwitchLanguageToRussian() {
        HomePage homePage = new HomePage(page);
        homePage.open(BASE_URL);
        homePage.selectLanguage("ru");

        Assert.assertEquals(homePage.getSelectedLanguage(), "ru",
                "Selected language should be Russian");
    }

    // Verify switching language to English

    @Test(description = "Verify switching language to English")
    public void shouldSwitchLanguageToEnglish() {
        HomePage homePage = new HomePage(page);
        homePage.open(BASE_URL);
        homePage.selectLanguage("ru");
        homePage.selectLanguage("en");

        Assert.assertEquals(homePage.getSelectedLanguage(), "en",
                "Selected language should be English");
    }

    // Verify selected language after file upload

    @Test(description = "Verify selected language remains Russian after file upload")
    public void shouldKeepSelectedLanguageAfterFileUpload() {
        HomePage homePage = new HomePage(page);
        homePage.open(BASE_URL);
        homePage.selectLanguage("ru");
        homePage.uploadFile(RUSSIAN_AUDIO);

        Assert.assertEquals(homePage.getSelectedLanguage(), "ru",
                "Selected language should remain Russian after file upload");
    }

    // Verify changing language after file upload

    @Test(description = "Verify language can be changed after file upload")
    public void shouldAllowChangingLanguageAfterFileUpload() {
        HomePage homePage = new HomePage(page);
        homePage.open(BASE_URL);
        homePage.uploadFile(ENGLISH_AUDIO);
        homePage.selectLanguage("ru");

        Assert.assertEquals(homePage.getSelectedLanguage(), "ru",
                "Language should be changeable after file upload");
        Assert.assertEquals(homePage.getSelectedFileName(), "sample-en.mp3",
                "Selected file should remain unchanged");
    }

    // Verify empty audio file validation

    @Test(description = "Verify empty audio file is rejected")
    public void shouldRejectEmptyAudioFile() throws IOException {
        Path emptyFile = Files.createTempFile("empty-audio-", ".mp3");

        try {
            HomePage homePage = new HomePage(page);
            homePage.open(BASE_URL);
            homePage.uploadFile(emptyFile);
            homePage.waitForProcessingNote("File is empty.");

            Assert.assertEquals(homePage.getProcessingNoteText(), "File is empty.",
                    "Empty file validation message does not match");
            Assert.assertFalse(homePage.isTranscribeButtonEnabled(),
                    "Transcribe button should remain disabled for an empty file");
        } finally {
            Files.deleteIfExists(emptyFile);
        }
    }

    // Verify unsupported file format

    @Test(description = "Verify unsupported file format is rejected")
    public void shouldRejectUnsupportedFileFormat() throws IOException {
        Path unsupportedFile = Files.createTempFile("unsupported-file-", ".txt");
        Files.writeString(unsupportedFile, "This is not an audio file.");

        try {
            HomePage homePage = new HomePage(page);
            homePage.open(BASE_URL);
            homePage.uploadFile(unsupportedFile);

            String expectedMessage =
                    "Unsupported file format. Use MP3, WAV, M4A, MP4 or WEBM.";
            homePage.waitForProcessingNote(expectedMessage);

            Assert.assertEquals(homePage.getProcessingNoteText(), expectedMessage,
                    "Unsupported format error message should be displayed.");
            Assert.assertFalse(homePage.isTranscribeButtonEnabled(),
                    "Transcribe button should remain disabled.");
        } finally {
            Files.deleteIfExists(unsupportedFile);
        }
    }

    // Verify oversized file validation

    @Test(description = "Verify file larger than 50 MB is rejected")
    public void shouldRejectOversizedFile() throws IOException {
        Path oversizedFile = Files.createTempFile("oversized-audio-", ".mp3");

        try (RandomAccessFile file = new RandomAccessFile(oversizedFile.toFile(), "rw")) {
            file.setLength(50L * 1024 * 1024 + 1);
        }

        try {
            HomePage homePage = new HomePage(page);
            homePage.open(BASE_URL);
            homePage.uploadFile(oversizedFile);

            String expectedMessage = "File size exceeds the 50 MB limit.";
            homePage.waitForProcessingNote(expectedMessage);
            homePage.waitForTranscribeButtonState(false);

            Assert.assertEquals(homePage.getProcessingNoteText(), expectedMessage);
            Assert.assertFalse(homePage.isTranscribeButtonEnabled());
        } finally {
            Files.deleteIfExists(oversizedFile);
        }
    }

    // Verify audio longer than five minutes is rejected

    @Test(description = "Verify audio longer than five minutes is rejected")
    public void shouldRejectAudioLongerThanFiveMinutes() throws Exception {
        Path longAudioFile = Files.createTempFile("long-audio-", ".wav");

        try {
            AudioFormat format = new AudioFormat(8000, 8, 1, true, false);
            int durationSeconds = 301;
            int audioLength = 8000 * durationSeconds;
            byte[] audioData = new byte[audioLength];

            try (AudioInputStream audioStream = new AudioInputStream(
                    new ByteArrayInputStream(audioData), format, audioLength)) {
                AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE,
                        longAudioFile.toFile());
            }

            HomePage homePage = new HomePage(page);
            homePage.open(BASE_URL);
            homePage.uploadFile(longAudioFile);

            String expectedMessage = "File duration exceeds the 5-minute limit.";
            homePage.waitForProcessingNote(expectedMessage);
            homePage.waitForTranscribeButtonState(false);

            Assert.assertEquals(homePage.getProcessingNoteText(), expectedMessage);
            Assert.assertFalse(homePage.isTranscribeButtonEnabled());
        } finally {
            Files.deleteIfExists(longAudioFile);
        }
    }

    // Verify behavior when media duration cannot be detected

    @Test(description = "Verify behavior when media duration cannot be detected")
    public void shouldHandleUnavailableMediaDuration() throws IOException {
        Path invalidAudioFile = Files.createTempFile("invalid-audio-", ".mp3");
        Files.writeString(invalidAudioFile, "Invalid audio content");

        try {
            HomePage homePage = new HomePage(page);
            homePage.open(BASE_URL);
            homePage.uploadFile(invalidAudioFile);

            String expectedMessage =
                    "Could not read media duration. The server will validate the file.";
            homePage.waitForProcessingNote(expectedMessage);
            homePage.waitForTranscribeButtonState(true);

            Assert.assertEquals(homePage.getProcessingNoteText(), expectedMessage);
            Assert.assertTrue(homePage.isTranscribeButtonEnabled());
        } finally {
            Files.deleteIfExists(invalidAudioFile);
        }
    }

    // Verify the server error is shown when a corrupted file is submitted

    @Test(description = "Verify server error is shown for a corrupted audio file")
    public void shouldShowServerErrorForCorruptedAudio() throws IOException {
        Path invalidAudioFile = Files.createTempFile("corrupted-audio-", ".mp3");
        Files.writeString(invalidAudioFile, "Invalid audio content");

        try {
            HomePage homePage = new HomePage(page);
            homePage.open(BASE_URL);
            homePage.uploadFile(invalidAudioFile);
            homePage.waitForTranscribeButtonState(true);
            homePage.startTranscription();

            String expectedMessage = "Audio processing failed.";
            homePage.waitForProcessingNote(expectedMessage);

            Assert.assertEquals(homePage.getProcessingNoteText(), expectedMessage,
                    "Server error should be displayed to the user.");
        } finally {
            Files.deleteIfExists(invalidAudioFile);
        }
    }

    // Verify transcription button is enabled after valid file selection

    @Test(description = "Verify transcription button is enabled after valid file selection")
    public void shouldEnableTranscriptionAfterValidFileSelection() {
        HomePage homePage = new HomePage(page);
        homePage.open(BASE_URL);
        homePage.uploadFile(ENGLISH_AUDIO);
        homePage.waitForTranscribeButtonState(true);

        Assert.assertTrue(homePage.isTranscribeButtonEnabled(),
                "Transcribe button should be enabled after valid file selection");
    }

    // Verify transcription button is disabled after invalid file selection

    @Test(description = "Verify transcription button is disabled after invalid file selection")
    public void shouldDisableTranscriptionAfterInvalidFileSelection() throws IOException {
        Path unsupportedFile = Files.createTempFile("unsupported-file-", ".txt");
        Files.writeString(unsupportedFile, "Unsupported content");

        try {
            HomePage homePage = new HomePage(page);
            homePage.open(BASE_URL);
            homePage.uploadFile(unsupportedFile);
            homePage.waitForTranscribeButtonState(false);

            Assert.assertFalse(homePage.isTranscribeButtonEnabled(),
                    "Transcribe button should be disabled for unsupported files");
        } finally {
            Files.deleteIfExists(unsupportedFile);
        }
    }

    // Verify validation error disappears after selecting valid file

    @Test(description = "Verify validation error disappears after selecting valid file")
    public void shouldClearValidationErrorAfterValidFileSelection() throws IOException {
        Path unsupportedFile = Files.createTempFile("unsupported-file-", ".txt");
        Files.writeString(unsupportedFile, "Unsupported content");

        try {
            HomePage homePage = new HomePage(page);
            homePage.open(BASE_URL);
            homePage.uploadFile(unsupportedFile);

            String errorMessage =
                    "Unsupported file format. Use MP3, WAV, M4A, MP4 or WEBM.";
            homePage.waitForProcessingNote(errorMessage);

            homePage.uploadFile(ENGLISH_AUDIO);
            homePage.waitForTranscribeButtonState(true);

            Assert.assertNotEquals(homePage.getProcessingNoteText(), errorMessage,
                    "Previous validation error should disappear");
            Assert.assertTrue(homePage.isTranscribeButtonEnabled());
        } finally {
            Files.deleteIfExists(unsupportedFile);
        }
    }
}