package com.aureva.transcribe.tests.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import java.nio.file.Path;

public class HomePage {

    private final Page page;

    // Page locators

    private final Locator brand;
    private final Locator pageTitle;
    private final Locator uploadFileTab;
    private final Locator fileInput;
    private final Locator selectedFileName;
    private final Locator languageSelect;
    private final Locator transcribeButton;
    private final Locator transcriptContent;
    private final Locator historyBody;
    private final Locator processingNote;

    // Constructor

    public HomePage(Page page) {
        this.page = page;

        this.brand = page.getByTestId("brand-home");
        this.pageTitle = page.locator(".hero-copy h1");
        this.uploadFileTab = page.getByTestId("tab-upload-file");
        this.fileInput = page.getByTestId("media-file-input");
        this.selectedFileName = page.getByTestId("selected-file-name");
        this.languageSelect = page.getByTestId("language-select");
        this.transcribeButton = page.getByTestId("transcribe-button");
        this.transcriptContent = page.locator("#transcript-content");
        this.historyBody = page.locator("#history-body");
        this.processingNote = page.locator("#processing-note");
    }

    // Open home page

    public void open(String url) {
        page.navigate(url);
    }

    // Upload media file

    public void uploadFile(Path filePath) {
        fileInput.setInputFiles(filePath);
    }

    // Select transcription language

    public void selectLanguage(String language) {
        languageSelect.selectOption(language);
    }

    // Start transcription

    public void startTranscription() {
        transcribeButton.click();
    }

    // Get selected file name

    public String getSelectedFileName() {
        return selectedFileName.textContent().trim();
    }

    // Get page title

    public String getPageTitle() {
        return pageTitle.textContent().trim();
    }

    // Get transcript text

    public String getTranscriptText() {
        return transcriptContent.textContent().trim();
    }

    // Get history row count

    public int getHistoryCount() {
        return historyBody.locator("tr").count();
    }

    // Check transcription button state

    public boolean isTranscribeButtonEnabled() {
        return transcribeButton.isEnabled();
    }

    // Check brand visibility

    public boolean isBrandVisible() {
        return brand.isVisible();
    }

    // Check upload tab visibility

    public boolean isUploadTabVisible() {
        return uploadFileTab.isVisible();
    }

    // Check transcription button visibility

    public boolean isTranscribeButtonVisible() {
        return transcribeButton.isVisible();
    }

    // Transcribe English audio

    public String transcribeEnglishAudio(Path filePath) {
        return transcribeAudio(filePath, "en");
    }

    // Transcribe Russian audio

    public String transcribeRussianAudio(Path filePath) {
        return transcribeAudio(filePath, "ru");
    }

    // Execute transcription workflow

    private String transcribeAudio(Path filePath, String language) {
        uploadFile(filePath);
        selectLanguage(language);
        startTranscription();

        return waitForTranscript();
    }

    // Wait for transcript or processing error

    public String waitForTranscript() {
        page.waitForFunction(
                """
                () => {
                    const transcript = document.querySelector(
                        '#transcript-content p:not(.transcript-placeholder)'
                    );

                    const button = document.querySelector(
                        '[data-testid="transcribe-button"]'
                    );

                    return (
                        (transcript && transcript.textContent.trim().length > 0) ||
                        (button && !button.disabled)
                    );
                }
                """,
                null,
                new Page.WaitForFunctionOptions()
                        .setTimeout(300_000)
        );

        Locator transcript = page.locator(
                "#transcript-content p:not(.transcript-placeholder)"
        );

        if (transcript.count() > 0) {
            String transcriptText = transcript.textContent().trim();

            if (!transcriptText.isEmpty()) {
                return transcriptText;
            }
        }

        String errorMessage = getProcessingNoteText();

        if (errorMessage.isBlank()) {
            errorMessage = "Transcription failed without an error message.";
        }

        throw new PlaywrightException(
                "Transcription failed: " + errorMessage
        );
    }

    // Get current URL

    public String getCurrentUrl() {
        return page.url();
    }

    // Get selected language

    public String getSelectedLanguage() {
        return languageSelect.inputValue();
    }

    // Check transcript visibility

    public boolean isTranscriptVisible() {
        return transcriptContent.isVisible();
    }

    // Get history text

    public String getHistoryText() {
        return historyBody.textContent().trim();
    }

    // Check if history contains a filename

    public boolean historyContainsFile(String fileName) {
        return historyBody.getByText(fileName).count() > 0;
    }

    // Get processing note text

    public String getProcessingNoteText() {
        return processingNote.textContent().trim();
    }

    // Wait for processing note

    public void waitForProcessingNote(String expectedMessage) {
        page.waitForFunction(
                """
                (expectedMessage) => {
                    const element = document.querySelector(
                        '#processing-note'
                    );

                    return element &&
                           element.textContent.trim() === expectedMessage;
                }
                """,
                expectedMessage
        );
    }

    // Wait for transcription button state

    public void waitForTranscribeButtonState(boolean enabled) {
        page.waitForFunction(
                """
                (expectedEnabled) => {
                    const button = document.querySelector(
                        '[data-testid="transcribe-button"]'
                    );

                    return button &&
                           button.disabled === !expectedEnabled;
                }
                """,
                enabled
        );
    }
}
