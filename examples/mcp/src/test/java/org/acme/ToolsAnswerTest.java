package org.acme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkiverse.mcp.server.test.McpAssured.McpStreamableTestClient;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ToolsAnswerTest {

    @TestHTTPResource
    URI testUri;

    @Test
    public void testAnswer() {
        McpStreamableTestClient client = McpAssured
                .newStreamableClient()
                .setBaseUri(testUri)
                .build()
                .connect();

        client.when()
                .toolsCall("theAnswer", Map.of("lang", "Java"), r -> {
                    assertEquals("Spaces are better for indentation.", r.content().get(0).asText().text());
                })
                .toolsCall("theAnswer", Map.of("lang", "python"), r -> {
                    assertEquals("Tabs are better for indentation.", r.content().get(0).asText().text());
                })
                .thenAssertResults();
    }

}
