package com.aureva.transcribe.tests.base;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class BaseTest extends SuiteDataCleanup {

    // The browser is started once per test class; every test gets a fresh context.

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    @BeforeClass(alwaysRun = true)
    public void launchBrowser() {
        playwright = Playwright.create();

        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(true)
        );
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        context = browser.newContext();

        page = context.newPage();

        page.setDefaultTimeout(10_000);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE && page != null) {
            saveScreenshot(result);
        }

        if (context != null) {
            context.close();
        }
    }

    @AfterClass(alwaysRun = true)
    public void closeBrowser() {
        if (browser != null) {
            browser.close();
        }

        if (playwright != null) {
            playwright.close();
        }
    }

    // Screenshots of failed tests are saved to target/screenshots

    private void saveScreenshot(ITestResult result) {
        try {
            Path file = Path.of(
                    "target",
                    "screenshots",
                    result.getTestClass().getRealClass().getSimpleName()
                            + "-" + result.getMethod().getMethodName() + ".png"
            );

            Files.createDirectories(file.getParent());

            page.screenshot(new Page.ScreenshotOptions().setPath(file));
        } catch (IOException | RuntimeException exception) {
            System.err.println("Could not save screenshot: " + exception.getMessage());
        }
    }
}