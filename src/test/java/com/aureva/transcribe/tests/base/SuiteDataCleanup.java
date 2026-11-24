package com.aureva.transcribe.tests.base;

import com.aureva.transcribe.tests.api.clients.HistoryApiClient;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Removes the history records created by tests.
 *
 * <p>Before the suite the existing record ids are remembered; after the suite
 * only the records that were added during the run are deleted, so real data is
 * never touched.
 */
public abstract class SuiteDataCleanup {

    private static final HistoryApiClient HISTORY = new HistoryApiClient();

    private static Set<Long> idsBeforeSuite;

    @BeforeSuite(alwaysRun = true)
    public void rememberExistingHistory() {
        if (idsBeforeSuite != null) {
            return;
        }

        try {
            idsBeforeSuite = fetchIds();
        } catch (RuntimeException exception) {
            // The application may be unavailable; cleanup is skipped safely.
            idsBeforeSuite = null;
        }
    }

    @AfterSuite(alwaysRun = true)
    public void removeRecordsCreatedByTests() {
        if (idsBeforeSuite == null) {
            return;
        }

        try {
            List<Long> created = fetchIds().stream()
                    .filter(id -> !idsBeforeSuite.contains(id))
                    .toList();

            if (!created.isEmpty()) {
                HISTORY.deleteSelectedRecords(created);
            }
        } catch (RuntimeException exception) {
            System.err.println("History cleanup failed: " + exception.getMessage());
        } finally {
            idsBeforeSuite = null;
        }
    }

    private static Set<Long> fetchIds() {
        List<?> rawIds = HISTORY.getHistory().jsonPath().getList("id");
        Set<Long> ids = new HashSet<>();

        for (Object rawId : rawIds) {
            ids.add(((Number) rawId).longValue());
        }

        return ids;
    }
}
