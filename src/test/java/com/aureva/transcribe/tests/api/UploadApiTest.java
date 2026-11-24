package com.aureva.transcribe.tests.api;

import com.aureva.transcribe.tests.api.clients.HistoryApiClient;
import com.aureva.transcribe.tests.api.clients.UploadApiClient;
import com.aureva.transcribe.tests.base.SuiteDataCleanup;
import com.aureva.transcribe.tests.utils.TestFiles;
import com.aureva.transcribe.tests.utils.TranscriptChecks;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class UploadApiTest extends SuiteDataCleanup {

    private final UploadApiClient uploadApiClient = new UploadApiClient();
    private final HistoryApiClient historyApiClient = new HistoryApiClient();

    // Validation

    @Test
    public void shouldReturnBadRequestWhenFileIsMissing() {
        Response response = uploadApiClient.uploadWithoutFile();

        response.then()
                .statusCode(400)
                .body("error", equalTo("Please select a file."));
    }

    @Test
    public void shouldReturnBadRequestWhenFileFormatIsUnsupported() {
        Response response = uploadApiClient.uploadFile(
                "sample.txt",
                "test content".getBytes(),
                "text/plain"
        );

        response.then()
                .statusCode(400)
                .body("error", equalTo("Unsupported file format."));
    }

    @Test
    public void shouldReturnBadRequestWhenFileIsEmpty() {
        Response response = uploadApiClient.uploadFile(
                "empty.mp3",
                new byte[0],
                "audio/mpeg"
        );

        response.then()
                .statusCode(400)
                .body("error", equalTo("Please select a file."));
    }

    @Test
    public void shouldReturnPayloadTooLargeWhenFileExceedsLimit() {
        Response response = uploadApiClient.uploadOversizedFile();

        response.then()
                .statusCode(413);
    }

    @Test
    public void shouldReturnBadRequestWhenFilenameIsMissing() {
        Response response = uploadApiClient.uploadFileWithoutFilename();

        response.then()
                .statusCode(400)
                .body("error", equalTo("File name is missing."));
    }

    @Test
    public void shouldReturnBadRequestWhenFilenameExceeds255Characters() {
        String fileName = "a".repeat(252) + ".mp3";

        Response response = uploadApiClient.uploadFile(
                fileName,
                "test content".getBytes(),
                "audio/mpeg"
        );

        response.then()
                .statusCode(400)
                .body("error", equalTo("File name is too long."));
    }

    @Test
    public void shouldReturnBadRequestWhenLanguageIsUnsupported() {
        Response response = uploadApiClient.uploadFileWithLanguage(
                "sample.mp3",
                "test content".getBytes(),
                "audio/mpeg",
                "de"
        );

        response.then()
                .statusCode(400)
                .body("error", equalTo("Unsupported language."));
    }

    // Server-side processing errors

    @Test
    public void shouldReturnUnprocessableEntityWhenAudioIsCorrupted() {
        Response response = uploadApiClient.uploadFileWithLanguage(
                "corrupted.mp3",
                "this is not audio".getBytes(),
                "audio/mpeg",
                "en"
        );

        response.then()
                .statusCode(422)
                .body("error", equalTo("Audio processing failed."));
    }

    @Test
    public void shouldReturnBadRequestWhenAudioIsLongerThanFiveMinutes()
            throws Exception {

        Response response = uploadApiClient.uploadFileWithLanguage(
                "long-silence.wav",
                TestFiles.silentWav(310),
                "audio/wav",
                "en"
        );

        response.then()
                .statusCode(400)
                .body("error", equalTo("File duration exceeds the 5-minute limit."));
    }

    @Test
    public void shouldNotSaveHistoryRecordWhenProcessingFails() {
        int before = historyApiClient.getHistory().jsonPath().getList("$").size();

        uploadApiClient.uploadFileWithLanguage(
                "corrupted-no-history.mp3",
                "this is not audio".getBytes(),
                "audio/mpeg",
                "en"
        ).then().statusCode(422);

        int after = historyApiClient.getHistory().jsonPath().getList("$").size();

        assertEquals(after, before, "Failed upload must not create a history record.");
    }

    // Successful transcription

    @Test
    public void shouldUseDefaultLanguageWhenLanguageIsEmpty() throws Exception {
        Response response = uploadApiClient.uploadFileWithLanguage(
                "sample-en.mp3",
                TestFiles.audioBytes("sample-en.mp3"),
                "audio/mpeg",
                ""
        );

        response.then()
                .statusCode(200)
                .body("language", equalTo("en"))
                .body("transcript", not(emptyOrNullString()));
    }

    @Test
    public void shouldSuccessfullyTranscribeEnglishAudioWithUppercaseLanguage()
            throws Exception {

        Response response = uploadApiClient.uploadFileWithLanguage(
                "sample-en.mp3",
                TestFiles.audioBytes("sample-en.mp3"),
                "audio/mpeg",
                "EN"
        );

        response.then()
                .statusCode(200)
                .body("language", equalTo("en"))
                .body("fileName", equalTo("sample-en.mp3"))
                .body("transcript", not(emptyOrNullString()));

        assertTrue(
                TranscriptChecks.isEnglish(response.jsonPath().getString("transcript")),
                "English transcript should contain Latin letters only."
        );
    }

    @Test
    public void shouldSuccessfullyTranscribeRussianAudio() throws Exception {
        Response response = uploadApiClient.uploadFileWithLanguage(
                "sample-ru.mp3",
                TestFiles.audioBytes("sample-ru.mp3"),
                "audio/mpeg",
                "ru"
        );

        response.then()
                .statusCode(200)
                .body("language", equalTo("ru"))
                .body("fileName", equalTo("sample-ru.mp3"))
                .body("transcript", not(emptyOrNullString()));

        assertTrue(
                TranscriptChecks.isRussian(response.jsonPath().getString("transcript")),
                "Russian transcript should contain Cyrillic letters only."
        );
    }

    @Test
    public void shouldAcceptUppercaseFileExtension() throws Exception {
        Response response = uploadApiClient.uploadFileWithLanguage(
                "SAMPLE-UPPERCASE.MP3",
                TestFiles.audioBytes("sample-en.mp3"),
                "audio/mpeg",
                "en"
        );

        response.then()
                .statusCode(200)
                .body("extension", equalTo("mp3"));
    }

    // Security

    @Test
    public void shouldStripPathFromFileName() throws Exception {
        Response response = uploadApiClient.uploadFileWithLanguage(
                "../../secret/sample-path.mp3",
                TestFiles.audioBytes("sample-en.mp3"),
                "audio/mpeg",
                "en"
        );

        response.then()
                .statusCode(200)
                .body("fileName", equalTo("sample-path.mp3"));
    }
}
