package com.aureva.transcribe.tests.ui;

import com.aureva.transcribe.tests.api.clients.UploadApiClient;
import com.aureva.transcribe.tests.base.BaseTest;
import com.aureva.transcribe.tests.config.TestConfig;
import com.aureva.transcribe.tests.pages.HistoryPage;
import com.aureva.transcribe.tests.utils.TestFiles;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.UUID;

public class HistoryPageTest extends BaseTest {

    private static final String BASE_URL = TestConfig.BASE_URL;

    private String seedFileName;

    // A known record is created through the API, so the tests
    // always check a non-empty history.

    @BeforeClass
    public void createSeedRecord() throws IOException {
        seedFileName = "history-ui-" + UUID.randomUUID() + ".mp3";

        new UploadApiClient().uploadFileWithLanguage(
                seedFileName,
                TestFiles.audioBytes("sample-en.mp3"),
                "audio/mpeg",
                "en"
        ).then().statusCode(200);
    }

    // Verify history page visibility

    @Test(description = "Verify history page is visible")
    public void shouldDisplayHistorySection() {

        page.navigate(BASE_URL);

        HistoryPage historyPage = new HistoryPage(page);

        historyPage.openHistory();

        Assert.assertTrue(
                historyPage.isHistoryVisible(),
                "History page should be visible"
        );
    }

    // Verify history title

    @Test(description = "Verify history title")
    public void shouldDisplayHistoryTitle() {

        page.navigate(BASE_URL);

        HistoryPage historyPage = new HistoryPage(page);

        historyPage.openHistory();

        Assert.assertFalse(
                historyPage.getHistoryTitle().isBlank(),
                "History title should not be empty"
        );
    }

    // Verify history content

    @Test(description = "Verify history content is displayed")
    public void shouldDisplayHistoryContent() {

        page.navigate(BASE_URL);

        HistoryPage historyPage = new HistoryPage(page);

        historyPage.openHistory();
        historyPage.waitForHistoryLoaded();

        Assert.assertFalse(
                historyPage.isHistoryEmpty(),
                "History should not be empty"
        );

        Assert.assertTrue(
                historyPage.getHistoryCount() > 0,
                "History should contain at least one record"
        );
    }

    // Verify a record created through the API is shown

    @Test(description = "Verify uploaded file appears in history")
    public void shouldDisplayUploadedFileInHistory() {

        page.navigate(BASE_URL);

        HistoryPage historyPage = new HistoryPage(page);

        historyPage.openHistory();
        historyPage.waitForHistoryLoaded();

        Assert.assertTrue(
                historyPage.containsFile(seedFileName),
                "Uploaded file should be listed in history: " + seedFileName
        );
    }

    // Verify View All link

    @Test(description = "Verify View All link is visible")
    public void shouldDisplayViewAllLink() {

        page.navigate(BASE_URL);

        HistoryPage historyPage = new HistoryPage(page);

        Assert.assertTrue(
                historyPage.isViewAllVisible(),
                "View All link should be visible on the home page"
        );
    }
}
