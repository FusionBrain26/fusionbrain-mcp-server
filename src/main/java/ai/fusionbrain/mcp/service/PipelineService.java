package ai.fusionbrain.mcp.service;

import ai.fusionbrain.mcp.dto.StartGenerateToolRequest;
import ai.fusionbrain.mcp.dto.StartGenerateToolResponse;
import ai.fusionbrain.mcp.dto.StatusToolResponse;

import java.util.UUID;

public interface PipelineService {
    StartGenerateToolResponse startGenerateImage(StartGenerateToolRequest request);

    StatusToolResponse checkStatus(UUID resourceId);

    StatusToolResponse generateImageSynchronously(StartGenerateToolRequest request);
}
