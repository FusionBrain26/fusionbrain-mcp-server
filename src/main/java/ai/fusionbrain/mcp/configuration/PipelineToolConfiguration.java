package ai.fusionbrain.mcp.configuration;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ai.fusionbrain.client.FusionBrainClient;
import ai.fusionbrain.mcp.service.PipelineService;
import ai.fusionbrain.mcp.service.PipelineServiceImpl;
import ai.fusionbrain.mcp.service.PipelineServiceStub;
import ai.fusionbrain.mcp.tool.PipelineToolService;

@Configuration
public class PipelineToolConfiguration {
    @Bean
    @ConditionalOnProperty(name = "fusionbrain.enabled", havingValue = "true")
    public PipelineService remotePipelineService(FusionBrainClient fusionBrainClient) {
        return new PipelineServiceImpl(fusionBrainClient);
    }

    @Bean
    @ConditionalOnProperty(name = "fusionbrain.enabled", havingValue = "false", matchIfMissing = true)
    public PipelineService pipelineServiceStub() {
        return new PipelineServiceStub();
    }

    @Bean
    public PipelineToolService pipelineToolService(PipelineService pipelineService) {
        return new PipelineToolService(pipelineService);
    }

    @Bean
    public ToolCallbackProvider toolCallbackProvider(
            PipelineToolService pipelineToolService
    ) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(pipelineToolService)
                .build();
    }
}
