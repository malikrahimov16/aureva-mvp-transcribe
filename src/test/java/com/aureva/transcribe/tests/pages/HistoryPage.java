package com.aureva.transcribe.tests.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public class HistoryPage {

    // History page locators — Локаторы истории

    private final Locator historyNavigation;
    private final Locator historySection;
    private final Locator historyTitle;
    private final Locator historyBody;
    private final Locator emptyHistoryMessage;
    private final Locator viewAllLink;

    // Constructor — Конструктор страницы

    public HistoryPage(Page page) {

        this.historyNavigation = page.getByTestId("nav-history");
        this.historySection = page.locator("#history-page");
        this.historyTitle = historySection.locator(".history-page-title");
        this.historyBody = historySection.locator("#history-page-body");
        this.emptyHistoryMessage = historyBody.locator(".empty-history");
        this.viewAllLink = page.getByTestId("history-view-all");
    }

    // Open history page — Открыть историю

    public void openHistory() {
        historyNavigation.click();
    }

    // Check history visibility — Проверить отображение истории

    public boolean isHistoryVisible() {
        return historySection.isVisible();
    }

    // Get history title — Получить заголовок истории

    public String getHistoryTitle() {
        return historyTitle.innerText().trim();
    }

    // Get number of history records — Получить количество записей

    public int getHistoryCount() {
        return historyBody.locator("tr:not(:has(.empty-history))").count();
    }

    // Check empty history — Проверить сообщение истории

    public boolean isHistoryEmpty() {
        return emptyHistoryMessage.isVisible();
    }

    // Get empty history message — Получить сообщение истории

    public String getEmptyHistoryMessage() {
        return emptyHistoryMessage.innerText().trim();
    }

    // Check View All link — Проверить ссылку View All

    public boolean isViewAllVisible() {
        return viewAllLink.isVisible();
    }
    // Check that the history table contains a file name

    public boolean containsFile(String fileName) {
        return historyBody.getByText(fileName).count() > 0;
    }

    public void waitForHistoryLoaded() {
        historyBody.locator("tr").first().waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
        );
    }

}
