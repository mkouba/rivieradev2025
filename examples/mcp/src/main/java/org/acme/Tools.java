package org.acme;

import io.quarkiverse.mcp.server.McpLog;
import io.quarkiverse.mcp.server.Sampling;
import io.quarkiverse.mcp.server.SamplingMessage;
import io.quarkiverse.mcp.server.SamplingRequest;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolCallException;
import io.smallrye.mutiny.Uni;

public class Tools {

    @Tool(description = """
            Answer the ultimate question to tabs vs. spaces
            """)
    String theAnswer(
            @ToolArg(description = "The programming language", defaultValue = "Java") String lang,
            McpLog log) {
        log.info("Let's try to answer the question for lang: %s", lang);
        if ("python".equalsIgnoreCase(lang)) {
            return "Tabs are better for indentation.";
        }
        return "Spaces are better for indentation.";
    }

    @Tool(description = "Just test the sampling feature")
    Uni<String> justTestSampling(Sampling sampling, String topic) {
        if (sampling.isSupported()) {
            SamplingRequest samplingRequest = sampling.requestBuilder()
                    .setMaxTokens(100)
                    .addMessage(
                            SamplingMessage.withUserRole(
                                    "Tell me more about " + topic))
                    .build();
            return samplingRequest
                    .send()
                    .map(response -> response.content().asText().text());
        } else {
            throw new ToolCallException("Sampling not supported");
        }
    }

}
