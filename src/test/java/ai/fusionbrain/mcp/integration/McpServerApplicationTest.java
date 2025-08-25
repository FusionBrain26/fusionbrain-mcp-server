package ai.fusionbrain.mcp.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import ai.fusionbrain.autoconfigure.FusionBrainAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ai.fusionbrain.mcp.integration.WireMockServerFactory.setupMockServerResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(FusionBrainAutoConfiguration.class)
public class McpServerApplicationTest {
    private static final String SERVER_HOST = "http://localhost:";
    private static WireMockServer wireMockServer;

    @LocalServerPort
    private int serverPort;

    @BeforeAll
    static void setUpClass() {
        wireMockServer = WireMockServerFactory.startServer();
    }

    @AfterAll
    static void tearDownClass() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        String baseUrl = "http://localhost:" + wireMockServer.port();
        registry.add("fusionbrain.base-url", () -> baseUrl);
    }

    @Test
    void callTool__ThenGeneratedImageReturned() throws Exception {
        setupMockServerResponse(wireMockServer);
        var transport = HttpClientSseClientTransport.builder(SERVER_HOST + serverPort).build();
        try (McpSyncClient client = McpClient.sync(transport).build()) {
            client.initialize();
            int availableToolsSize = client.listTools().tools().size();
            assertEquals(3, availableToolsSize);

            McpSchema.CallToolResult callToolResult = client.callTool(new McpSchema.CallToolRequest(
                            "generateImageSync",
                            "{\"request\": {\"prompt\": \"some prompt\"}}"
                    )
            );

            assertThat(callToolResult.content())
                    .hasSize(1)
                    .first()
                    .matches(result -> result.toString().contains("BASED_IMAGE"));
        }
    }
}
