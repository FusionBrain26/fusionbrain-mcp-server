package ai.fusionbrain.mcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import ai.fusionbrain.client.FusionBrainClient;
import ai.fusionbrain.dto.EResourceStatus;
import ai.fusionbrain.dto.PipelineDTO;
import ai.fusionbrain.dto.RunResponse;
import ai.fusionbrain.dto.StatusResponse;
import ai.fusionbrain.dto.request.EText2ImageType;
import ai.fusionbrain.dto.request.GenerateParams;
import ai.fusionbrain.dto.request.Text2ImageParams;
import ai.fusionbrain.mcp.dto.GenerateRequestStatus;
import ai.fusionbrain.mcp.dto.GenerateToolStatus;
import ai.fusionbrain.mcp.dto.StartGenerateToolRequest;
import ai.fusionbrain.mcp.dto.StartGenerateToolResponse;
import ai.fusionbrain.mcp.dto.StatusToolResponse;

import java.util.Optional;
import java.util.UUID;

import static ai.fusionbrain.dto.EPipelineType.TEXT2IMAGE;


@RequiredArgsConstructor
@Slf4j
public class PipelineServiceImpl implements PipelineService {
    private final FusionBrainClient client;

    @Override
    public StartGenerateToolResponse startGenerateImage(StartGenerateToolRequest request) {
        try {
            Optional<PipelineDTO> pipelineOptional = client.getPipelines(TEXT2IMAGE)
                    .stream()
                    .findFirst();

            if (pipelineOptional.isEmpty()) {
                log.error("No {} pipelines available", TEXT2IMAGE);
                return StartGenerateToolResponse.failed("No pipelines available");
            }

            PipelineDTO pipeline = pipelineOptional.get();

            if (pipeline.getStatus().isDisabled()) {
                log.error("Pipeline {} is in status {}", pipeline.getId(), pipeline.getStatus());
                return StartGenerateToolResponse.failed("Pipeline is not active now. Try again later");
            }

            Text2ImageParams params = prepareParams(request);
            RunResponse runResponse = client.runPipeline(pipeline.getId(), params, null);
            log.info("Resource {} is in status {}", runResponse.getId(), runResponse.getStatus());

            return StartGenerateToolResponse.started(runResponse.getId(), runResponse.getStatusTime());
        } catch (Exception e) {
            log.error("Failed to start generating image", e);
            return StartGenerateToolResponse.failed("Internal error");
        }
    }

    @Override
    public StatusToolResponse checkStatus(UUID taskId) {
        try {
            StatusResponse statusResponse = client.getStatus(taskId);
            return processStatusResponse(statusResponse);
        } catch (Exception e) {
            return new StatusToolResponse(GenerateToolStatus.INTERNAL_ERROR);
        }
    }

    @Override
    public StatusToolResponse generateImageSynchronously(StartGenerateToolRequest request) {
        StartGenerateToolResponse response = startGenerateImage(request);
        if (response.getStatus() == GenerateRequestStatus.STARTED) {
            StatusResponse statusResponse = client.waitForCompletionSync(response.getTaskId(), response.getStatusRequestDelay());
            return processStatusResponse(statusResponse);
        } else {
            return new StatusToolResponse(GenerateToolStatus.INTERNAL_ERROR);
        }
    }

    private StatusToolResponse processStatusResponse(StatusResponse statusResponse) {
        EResourceStatus resourceStatus = statusResponse.getStatus();
        if (resourceStatus == EResourceStatus.DONE) {
            JsonNode result = statusResponse.getResult();
            JsonNode node = result.get("files");
            if (node != null && node.isArray() && !node.isEmpty()) {
                ArrayNode arrayNode = (ArrayNode) node;
                String b64 = arrayNode.get(0).asText();
                return new StatusToolResponse(GenerateToolStatus.FINISHED, b64);
            } else {
                log.error("Files node is not an array for resource {}", statusResponse.getId());
                return new StatusToolResponse(GenerateToolStatus.INTERNAL_ERROR);
            }
        }

        if (resourceStatus.isFinal()) {
            log.error("Resource {} is in status {}", statusResponse.getId(), resourceStatus);
            return new StatusToolResponse(GenerateToolStatus.INTERNAL_ERROR);
        }
        return new StatusToolResponse(GenerateToolStatus.IN_PROCESS);
    }

    private Text2ImageParams prepareParams(StartGenerateToolRequest toolRequest) {
        return new Text2ImageParams(
                EText2ImageType.GENERATE,
                toolRequest.getWidth(),
                toolRequest.getHeight(),
                1,
                new GenerateParams(toolRequest.getPrompt()),
                null,
                toolRequest.getStyle()
        );
    }
}
