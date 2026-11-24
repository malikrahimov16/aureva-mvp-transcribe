package com.aureva.transcribe.tests.config;

/**
 * Central test configuration.
 *
 * <p>The base URL can be overridden: {@code mvn test -Dbase.url=http://localhost:9090}
 */
public final class TestConfig {

    public static final String BASE_URL =
            System.getProperty("base.url", "http://localhost:8080");

    private TestConfig() {
    }
}
