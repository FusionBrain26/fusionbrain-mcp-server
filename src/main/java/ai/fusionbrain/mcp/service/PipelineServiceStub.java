package ai.fusionbrain.mcp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import ai.fusionbrain.mcp.dto.GenerateToolStatus;
import ai.fusionbrain.mcp.dto.StartGenerateToolRequest;
import ai.fusionbrain.mcp.dto.StartGenerateToolResponse;
import ai.fusionbrain.mcp.dto.StatusToolResponse;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Slf4j
public class PipelineServiceStub implements PipelineService {
    private final ConcurrentHashMap<UUID, Instant> genStartedMap = new ConcurrentHashMap<>();
    private static final long GENERATION_DELAY_SECONDS = 1L;

    @Override
    public StartGenerateToolResponse startGenerateImage(StartGenerateToolRequest request) {
        UUID resourceId = UUID.randomUUID();
        genStartedMap.put(resourceId, Instant.now());
        return StartGenerateToolResponse.started(resourceId, GENERATION_DELAY_SECONDS);
    }

    @Override
    public StatusToolResponse checkStatus(UUID resourceId) {
        Instant genStartInstant = genStartedMap.get(resourceId);

        if (genStartInstant == null) {
            return new StatusToolResponse(GenerateToolStatus.INTERNAL_ERROR);
        }

        if (Instant.now().minusSeconds(GENERATION_DELAY_SECONDS).isAfter(genStartInstant)) {
            genStartedMap.remove(resourceId);
            return new StatusToolResponse(GenerateToolStatus.FINISHED, "something based");
        }
        return new StatusToolResponse(GenerateToolStatus.IN_PROCESS);
    }

    @Override
    public StatusToolResponse generateImageSynchronously(StartGenerateToolRequest request) {
        try {
            Thread.sleep(GENERATION_DELAY_SECONDS * 1000); // 1 секунда
            return new StatusToolResponse(GenerateToolStatus.FINISHED, "something based");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted");
        }
    }
}