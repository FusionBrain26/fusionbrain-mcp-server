package ai.fusionbrain.mcp.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static java.nio.charset.Charset.defaultCharset;
import static org.springframework.util.StreamUtils.copyToString;

public class WireMockServerFactory {

    public static WireMockServer startServer() {
        WireMockServer server = new WireMockServer(0);
        server.start();
        return server;
    }

    public static void setupMockServerResponse(WireMockServer server) throws IOException {
        server.stubFor(
                get(urlPathEqualTo("/key/api/v1/pipelines"))
                        .withQueryParam("type", equalTo("TEXT2IMAGE"))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody(copyToString(
                                        classPathResourceStream("responses/pipelines.json"),
                                        defaultCharset()
                                ))
                        )
        );

        server.stubFor(
                post(urlPathEqualTo("/key/api/v1/pipeline/run"))
                        .withQueryParam("pipeline_id", equalTo("010f229d-d4e6-44f5-8485-b5a6013b6412"))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody(copyToString(
                                        classPathResourceStream("responses/run-pipeline.json"),
                                        defaultCharset()
                                ))
                        )
        );

        server.stubFor(
                get(urlPathMatching("/key/api/v1/pipeline/status/[^/]+"))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody(copyToString(
                                        classPathResourceStream("responses/status.json"),
                                        defaultCharset()
                                ))
                        )
        );
    }

    private static InputStream classPathResourceStream(String resourcePath) {
        return WireMockServerFactory.class.getClassLoader().getResourceAsStream(resourcePath);
    }
}
