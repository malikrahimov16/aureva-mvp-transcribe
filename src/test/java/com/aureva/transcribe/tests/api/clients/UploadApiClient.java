package com.aureva.transcribe.tests.api.clients;

import com.aureva.transcribe.tests.config.TestConfig;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class UploadApiClient {

    private static final String BASE_URL = TestConfig.BASE_URL;

    public Response uploadWithoutFile() {
        return RestAssured.given()
                .baseUri(BASE_URL)
                .multiPart("file", "", new byte[0], "application/octet-stream")
                .when()
                .post("/api/uploads");
    }

    public Response uploadFile(String fileName, byte[] content, String contentType) {
        return RestAssured.given()
                .baseUri(BASE_URL)
                .multiPart("file", fileName, content, contentType)
                .when()
                .post("/api/uploads");
    }

    public Response uploadOversizedFile() {
        byte[] content = new byte[50 * 1024 * 1024 + 1];

        return RestAssured.given()
                .baseUri(BASE_URL)
                .multiPart("file", "large.mp3", content, "audio/mpeg")
                .when()
                .post("/api/uploads");
    }

    public Response uploadFileWithoutFilename() {
        return RestAssured.given()
                .baseUri(BASE_URL)
                .multiPart("file", "", "test content".getBytes(), "audio/mpeg")
                .when()
                .post("/api/uploads");
    }

    public Response uploadFileWithLanguage(
            String fileName,
            byte[] content,
            String contentType,
            String language
    ) {
        return RestAssured.given()
                .baseUri(BASE_URL)
                .multiPart("file", fileName, content, contentType)
                .multiPart("language", language)
                .when()
                .post("/api/uploads");
    }
}
