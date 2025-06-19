---
title: Building efficient MCP servers efficiently with Quarkus
---

## Building efficient MCP servers efficiently

### with Quarkus

#### Martin Kouba

Riviera DEV 2025

---

### Who is Martin?

- [~] Introvert
- [~] Fan of open source, not only in software
- [~] Software engineer at Ret Hat/IBM
- [~] Quarkus MCP server contributor

---

### What's the plan for today?

- [~] Oh no, MCP again?
- [~] Quarkus MCP server: history, goals & design
- [~] What does the API look like?
- [~] Advanced features
- [~] Quarkus MCP.Next?

---

### Part 1 - MCP

---

### MCP - Model Context Protocol

- [~] Open protocol (MIT License) for integration between LLM applications and external resources and tools
- [~] [Specification](https://modelcontextprotocol.io/specification/2025-03-26): `2024-11-05`, `2025-03-26` and `2025-06-18`
- [~] Official SDKs (Python, TypeScript, Rust, ...) 
- [~] MCP Inspector - testing tool for MCP servers

---

### MCP spec - missing pieces

- [~] Not backed by a foundation
- [~] No clear development/review process
- [~] No shared API
- [~] No TCK (Technology Compatibility Kit)

---

### MCP concepts

- [~] MCP client ⇆ MCP server
- [~] JSON-RPC 2.0 messages
- [~] Transport - how messages are sent and received
- [~] Server features: tools, prompts, resources
- [~] Client features: sampling, roots, (elicitation)

---

### MCP transports

- [~] MCP currently defines two standard transports for communication
- [~] `stdio` transport starts an MCP server as a subprocess and communicates over standard in/out
- [~] `http` transport; clients connect to a running HTTP server

---

### HTTP transport variants

- [~] "HTTP/SSE" transport introduced in `2024-11-05` (considered deprecated but still supported by most clients and servers)
- [~] "Streamable HTTP" introduced in `2025-03-26`
- [~] Is "Streamable HTTP" better than the legacy "HTTP/SSE"? I'm not so sure but...

---

### MCP summary

[~] ➵➵➵ Wild West ➵➵➵

---

### Part 2 - History, goals & design

---

### What's quarkus-mcp-server?

- [~] Quarkus extension that lives in Quarkiverse
- [~] [1.0.0.Alpha1](https://github.com/quarkiverse/quarkus-mcp-server/releases/tag/1.0.0.Alpha1) released in December 2024
- [~] [1.0.0](https://github.com/quarkiverse/quarkus-mcp-server/releases/tag/1.0.0) released in April 2025
- [~] The latest version is 1.3.0, released two weeks ago

---

### Goals

- [~] Unified declarative and programmatic API
- [~] To implement MCP server features (tools, prompts and resources)
- [~] Independent of the selected transport

---

### Supported transports

- [~] The `stdio` transport ✅
- [~] Both variants of the `http` transport ✅
- [~] "Resumability and Redelivery" for the Streamable HTTP is not supported yet ❌

---

### Design

- [~] Declarative API ➵ annotated business methods of CDI beans
- [~] Programmatic API ➵ `@Inject` manager CDI beans and use fluent API to register a feature

---

### Part 3 - Quarkus MCP server API

---

### Declarative API in action

```java[1: 1-15|6-8|9-11|12-15]
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;

public class McpFeatures {

   @Tool(description = """
         Answer the ultimate question to tabs vs. spaces
         """)
   String answer(
           @ToolArg(description = "The programming language")
           String lang) {
      if ("python".equals(lang)) {
        return "Tabs are better for indentation.";
      }
      return "Spaces are better for indentation.";
   }
}
```

---

### Let's try this!

Demo time! Let's use goose!

---

### Execution model

- [~] A server feature method may use blocking or non-blocking logic
- [~] Execution model is determined by the method signature and additional annotations such as `@Blocking`, `@NonBlocking`, `@RunOnVirtualThread` or `@Transactional`

---

### Reactive tool in action

```java[1: 1-16|7-9|10-12|13-16]
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.smallrye.mutiny.Uni;

public class McpFeatures {

   @Tool(description = """
         Answer the ultimate question to tabs vs. spaces
         """)
   Uni<String> answer(
                @ToolArg(description = "The programming language")
                String lang) {
      if ("python".equals(lang)) {
        return Uni.createFrom().item("Tabs are better for indentation.");
      }
      return Uni.createFrom().item("Spaces are better for indentation.");
   }
}
```

---

### Programmatic API in action

```java[1: 1-15|3-4|7-18]
public class McpFeatures {

    @Inject
    ToolManager toolManager; 

    @Startup 
    void addTools() {
       toolManager.newTool("answer") 
          .setDescription("Answer the ultimate question to tabs vs. spaces")
          .addArgument("lang", "The programming language", true, String.class)
          .setHandler(
             toolArgs -> {
                if ("python".equals(toolArgs.args().get("lang").toString())) {
                   return ToolResponse.success("Tabs are better for indentation.");
                }
                return ToolResponse.success("Spaces are better for indentation.");
             })
          .register(); 
    }
}
```

---

### Part 4 - More advanced features

---


### Traffic logging

- Log all JSON messages sent/received

```properties
quarkus.mcp.server.traffic-logging.enabled=true
quarkus.mcp.server.traffic-logging.text-limit=1500
```

---

### Dev UI

TODO add image

---

### Client logging 

- [~] Feature methods can accept `io.quarkiverse.mcp.server.McpLog` param
- [~] A utility class that can send log message notifications to a connected MCP client
- [~] There are also convenient methods that log the message first (using JBoss Logging) and afterwards send a notification

---

### Progress API

TODO

---

### Sampling

TODO

---

### Initial checks

TODO

---

### Filters

TODO

---

### Multiple server configurations

TODO

---

### Part 5 - Quarkus MCP.Next

- [~] Improve the testing story
- [~] WebSocket transport
- [~] Add JSON schema validation

---

### And that's all

Thank you!

---




