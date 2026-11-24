package com.aureva.transcribe.tests.api;

import com.aureva.transcribe.tests.api.clients.HistoryApiClient;
import com.aureva.transcribe.tests.api.clients.UploadApiClient;
import com.aureva.transcribe.tests.base.SuiteDataCleanup;
import com.aureva.transcribe.tests.utils.TestFiles;
import io.restassured.response.Response;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.instanceOf;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class HistoryApiTest extends SuiteDataCleanup {

    private static final long UNKNOWN_ID = Long.MAX_VALUE;

    private final HistoryApiClient historyApiClient = new HistoryApiClient();
    private final UploadApiClient uploadApiClient = new UploadApiClient();

    private String seedFileName;

    // Every test starts from a known state: at least one record exists.

    @BeforeClass
    public void createSeedRecord() throws IOException {
        seedFileName = uniqueName("history-seed");
        createRecord(seedFileName);
    }

    // GET /api/history

    @Test
    public void shouldReturnSuccessfulResponse() {
        Response response = historyApiClient.getHistory();

        response.then()
                .statusCode(200)
                .contentType("application/json");
    }

    @Test
    public void shouldReturnArrayOfHistoryRecords() {
        Response response = historyApiClient.getHistory();

        response.then()
                .statusCode(200)
                .body("$", instanceOf(List.class));
    }

    @Test
    public void shouldContainUploadedRecord() {
        Map<String, Object> record = findRecord(seedFileName);

        assertNotNull(record, "Uploaded file should appear in history.");
        assertEquals(record.get("language"), "en");
        assertFalse(
                record.get("transcript").toString().isBlank(),
                "Transcript should be stored."
        );
    }

    @Test
    public void shouldReturnCorrectStructureForEachHistoryRecord() {
        for (Map<String, Object> record : getRecords()) {
            assertTrue(record.containsKey("id"));
            assertTrue(record.containsKey("fileName"));
            assertTrue(record.containsKey("language"));
            assertTrue(record.containsKey("transcript"));
            assertTrue(record.containsKey("createdAt"));
        }
    }

    @Test
    public void shouldReturnNonNullValuesForHistoryRecords() {
        for (Map<String, Object> record : getRecords()) {
            assertNotNull(record.get("id"));
            assertNotNull(record.get("fileName"));
            assertNotNull(record.get("language"));
            assertNotNull(record.get("transcript"));
            assertNotNull(record.get("createdAt"));

            assertFalse(record.get("fileName").toString().isBlank());
            assertFalse(record.get("language").toString().isBlank());
        }
    }

    @Test
    public void shouldReturnHistorySortedByNewestFirst() {
        List<Map<String, Object>> records = getRecords();

        for (int i = 0; i < records.size() - 1; i++) {
            LocalDateTime currentDate = LocalDateTime.parse(
                    records.get(i).get("createdAt").toString()
            );

            LocalDateTime nextDate = LocalDateTime.parse(
                    records.get(i + 1).get("createdAt").toString()
            );

            assertTrue(
                    !currentDate.isBefore(nextDate),
                    "History records are not sorted by newest first."
            );
        }
    }

    @Test
    public void shouldReturnSupportedLanguagesForHistoryRecords() {
        for (Map<String, Object> record : getRecords()) {
            String language = record.get("language").toString();

            assertTrue(
                    language.equals("en") || language.equals("ru"),
                    "Unexpected language: " + language
            );
        }
    }

    // DELETE /api/history/{id}

    @Test
    public void shouldDeleteSingleRecord() throws IOException {
        String fileName = uniqueName("history-delete-one");
        long id = createRecord(fileName);

        historyApiClient.deleteHistoryRecord(id).then().statusCode(204);

        assertNull(findRecord(fileName), "Record should be deleted.");
    }

    @Test
    public void shouldReturnNotFoundWhenDeletingRecordTwice() throws IOException {
        long id = createRecord(uniqueName("history-delete-twice"));

        historyApiClient.deleteHistoryRecord(id).then().statusCode(204);
        historyApiClient.deleteHistoryRecord(id).then().statusCode(404);
    }

    @Test
    public void shouldReturnNotFoundWhenDeletingUnknownRecord() {
        historyApiClient.deleteHistoryRecord(UNKNOWN_ID)
                .then()
                .statusCode(404);
    }

    // DELETE /api/history/selected

    @Test
    public void shouldDeleteSelectedRecords() throws IOException {
        String firstName = uniqueName("history-selected-1");
        String secondName = uniqueName("history-selected-2");
        long firstId = createRecord(firstName);
        long secondId = createRecord(secondName);

        historyApiClient.deleteSelectedRecords(List.of(firstId, secondId))
                .then()
                .statusCode(204);

        assertNull(findRecord(firstName));
        assertNull(findRecord(secondName));
    }

    @Test
    public void shouldReturnBadRequestWhenSelectedIdsAreEmpty() {
        historyApiClient.deleteSelectedRecords(List.of())
                .then()
                .statusCode(400);
    }

    @Test
    public void shouldNotDeleteAnythingWhenSomeSelectedIdsDoNotExist()
            throws IOException {

        String fileName = uniqueName("history-selected-partial");
        long id = createRecord(fileName);

        historyApiClient.deleteSelectedRecords(List.of(id, UNKNOWN_ID))
                .then()
                .statusCode(404);

        assertNotNull(
                findRecord(fileName),
                "Existing record should stay when the request is rejected."
        );
    }

    // DELETE /api/history (destructive: wipes the whole history)

    // Runs last so it cannot remove data needed by the other tests.
    @Test(priority = 99)
    public void shouldDeleteAllHistory() {
        if (!Boolean.getBoolean("tests.allow.destructive")) {
            throw new SkipException(
                    "Destructive test. Use a disposable database and run with "
                            + "-Dtests.allow.destructive=true"
            );
        }

        historyApiClient.deleteAllHistory().then().statusCode(204);

        assertTrue(getRecords(false).isEmpty(), "History should be empty.");
    }

    // Helpers

    private static String uniqueName(String prefix) {
        return prefix + "-" + UUID.randomUUID() + ".mp3";
    }

    private long createRecord(String fileName) throws IOException {
        uploadApiClient.uploadFileWithLanguage(
                fileName,
                TestFiles.audioBytes("sample-en.mp3"),
                "audio/mpeg",
                "en"
        ).then().statusCode(200);

        Map<String, Object> record = findRecord(fileName);

        assertNotNull(record, "Created record was not found in history.");

        return ((Number) record.get("id")).longValue();
    }

    private Map<String, Object> findRecord(String fileName) {
        return getRecords(false).stream()
                .filter(record -> fileName.equals(record.get("fileName")))
                .findFirst()
                .orElse(null);
    }

    private List<Map<String, Object>> getRecords() {
        return getRecords(true);
    }

    private List<Map<String, Object>> getRecords(boolean mustNotBeEmpty) {
        List<Map<String, Object>> records =
                historyApiClient.getHistory().jsonPath().getList("$");

        if (mustNotBeEmpty) {
            assertFalse(
                    records.isEmpty(),
                    "History must contain at least one record for this check."
            );
        }

        return records;
    }
}
