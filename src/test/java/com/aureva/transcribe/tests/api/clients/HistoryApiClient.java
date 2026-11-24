package com.aureva.transcribe.tests.api.clients;

import com.aureva.transcribe.tests.config.TestConfig;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.List;

public class HistoryApiClient {

    private static final String BASE_URL = TestConfig.BASE_URL;
    private static final String HISTORY_ENDPOINT = "/api/history";

    // Получить всю историю
    public Response getHistory() {
        return RestAssured.given()
                .baseUri(BASE_URL)
                .accept("application/json")
                .when()
                .get(HISTORY_ENDPOINT);
    }

    // Удалить одну запись по ID
    public Response deleteHistoryRecord(Long id) {
        return RestAssured.given()
                .baseUri(BASE_URL)
                .when()
                .delete(HISTORY_ENDPOINT + "/" + id);
    }

    // Удалить выбранные записи
    public Response deleteSelectedRecords(List<Long> ids) {
        return RestAssured.given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body(ids.toString())
                .when()
                .delete(HISTORY_ENDPOINT + "/selected");
    }

    // Удалить всю историю
    public Response deleteAllHistory() {
        return RestAssured.given()
                .baseUri(BASE_URL)
                .when()
                .delete(HISTORY_ENDPOINT);
    }
}
