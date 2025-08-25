package ai.fusionbrain.mcp.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;

import ai.fusionbrain.mcp.dto.StartGenerateToolRequest;
import ai.fusionbrain.mcp.dto.StartGenerateToolResponse;
import ai.fusionbrain.mcp.dto.StatusToolResponse;
import ai.fusionbrain.mcp.service.PipelineService;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class PipelineToolService {
    private final PipelineService pipelineService;

    @Tool(description = "Start generating image")
    public StartGenerateToolResponse startGenerateImage(StartGenerateToolRequest request) {
        return pipelineService.startGenerateImage(request);
    }

    @Tool(description = "Get status of generation")
    public StatusToolResponse checkStatus(String taskId) {
        return pipelineService.checkStatus(UUID.fromString(taskId));
    }

    @Tool(description = "Generate image synchronously waiting for result")
    public StatusToolResponse generateImageSync(StartGenerateToolRequest request) {
        return pipelineService.generateImageSynchronously(request);
    }
}
