package ai.fusionbrain.mcp.service;

import ai.fusionbrain.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

import ai.fusionbrain.client.FusionBrainClient;
import ai.fusionbrain.dto.request.Text2ImageParams;
import ai.fusionbrain.mcp.dto.GenerateRequestStatus;
import ai.fusionbrain.mcp.dto.GenerateToolStatus;
import ai.fusionbrain.mcp.dto.StartGenerateToolRequest;
import ai.fusionbrain.mcp.dto.StartGenerateToolResponse;
import ai.fusionbrain.mcp.dto.StatusToolResponse;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class PipelineServiceTest {
    private static final UUID PIPELINE_ID = UUID.randomUUID();
    private static final UUID TASK_ID = UUID.randomUUID();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private FusionBrainClient fusionBrainClient;
    private PipelineServiceImpl pipelineService;

    @BeforeEach
    void setUp() {
        fusionBrainClient = mock(FusionBrainClient.class);
        pipelineService = new PipelineServiceImpl(fusionBrainClient);
    }

    @Test
    void whenNoAvailablePipelines__ThenErrorResponse() {
        when(fusionBrainClient.getPipelines(any())).thenReturn(Collections.emptyList());

        StartGenerateToolResponse response = pipelineService.startGenerateImage(sampleToolRequest());

        assertThat(response)
                .isNotNull()
                .matches(rs -> rs.getStatus() == GenerateRequestStatus.ERROR)
                .matches(rs -> rs.getTaskId() == null)
                .extracting(StartGenerateToolResponse::getDescription)
                .isEqualTo("No pipelines available");
        verify(fusionBrainClient).getPipelines(eq(EPipelineType.TEXT2IMAGE));
        verifyNoMoreInteractions(fusionBrainClient);
    }

    @Test
    void whenPipelineDisabled__ThenErrorResponse() {
        when(fusionBrainClient.getPipelines(any())).thenReturn(List.of(disabledPipeline()));

        StartGenerateToolResponse response = pipelineService.startGenerateImage(sampleToolRequest());

        assertThat(response)
                .isNotNull()
                .matches(rs -> rs.getStatus() == GenerateRequestStatus.ERROR)
                .matches(rs -> rs.getTaskId() == null)
                .extracting(StartGenerateToolResponse::getDescription).asString()
                .startsWith("Pipeline is not active now");
        verify(fusionBrainClient).getPipelines(eq(EPipelineType.TEXT2IMAGE));
        verifyNoMoreInteractions(fusionBrainClient);
    }

    @Test
    void whenActivePipelineFound__ThenTaskStartedAndHasId() {
        when(fusionBrainClient.getPipelines(any())).thenReturn(List.of(activePipeline()));
        when(fusionBrainClient.runPipeline(any(), any(), any())).thenReturn(startedResponse());

        StartGenerateToolResponse response = pipelineService.startGenerateImage(sampleToolRequest());

        assertThat(response)
                .isNotNull()
                .matches(rs -> rs.getStatus() == GenerateRequestStatus.STARTED)
                .matches(rs -> rs.getDescription() == null)
                .extracting(StartGenerateToolResponse::getTaskId)
                .isEqualTo(TASK_ID);

        verify(fusionBrainClient).getPipelines(eq(EPipelineType.TEXT2IMAGE));
        verify(fusionBrainClient).runPipeline(eq(PIPELINE_ID), argThat(matchesPrompt("Sample prompt")), isNull());
        verifyNoMoreInteractions(fusionBrainClient);
    }

    @Test
    void whenTaskProcessing__ThenResponseWithNoResult() {
        when(fusionBrainClient.getStatus(any())).thenReturn(processingResponse());

        StatusToolResponse response = pipelineService.checkStatus(TASK_ID);

        assertThat(response)
                .isNotNull()
                .matches(rs -> rs.getStatus() == GenerateToolStatus.IN_PROCESS)
                .matches(rs -> rs.getResult() == null);

        verify(fusionBrainClient).getStatus(eq(TASK_ID));
        verifyNoMoreInteractions(fusionBrainClient);
    }

    @Test
    void whenTaskFinished__ThenResponseWithMatchingResult() {
        when(fusionBrainClient.getStatus(any())).thenReturn(finishedResponse());

        StatusToolResponse response = pipelineService.checkStatus(TASK_ID);

        assertThat(response)
                .isNotNull()
                .matches(rs -> rs.getStatus() == GenerateToolStatus.FINISHED)
                .matches(rs -> rs.getResult().equals("Sample result"));

        verify(fusionBrainClient).getStatus(eq(TASK_ID));
        verifyNoMoreInteractions(fusionBrainClient);
    }

    private StartGenerateToolRequest sampleToolRequest() {
        var toolRequest = new StartGenerateToolRequest();
        toolRequest.setPrompt("Sample prompt");
        return toolRequest;
    }

    private PipelineDTO disabledPipeline() {
        var pipelineDTO = new PipelineDTO();
        pipelineDTO.setId(PIPELINE_ID);
        pipelineDTO.setStatus(EPipelineStatus.DISABLED_BY_QUEUE);
        return pipelineDTO;
    }

    private PipelineDTO activePipeline() {
        var pipelineDTO = new PipelineDTO();
        pipelineDTO.setId(PIPELINE_ID);
        pipelineDTO.setStatus(EPipelineStatus.ACTIVE);
        return pipelineDTO;
    }

    private RunResponse startedResponse() {
        return new RunResponse(TASK_ID, EResourceStatus.INITIAL, EPipelineStatus.ACTIVE, -1L);
    }

    private StatusResponse finishedResponse() {
        var resultNode = objectMapper.createObjectNode();
        resultNode.putArray("files").add("Sample result");
        return new StatusResponse(
                TASK_ID,
                EResourceStatus.DONE,
                "Desc",
                resultNode,
                -1L
        );
    }

    private StatusResponse processingResponse() {
        return new StatusResponse(
                TASK_ID,
                EResourceStatus.PROCESSING,
                "Desc",
                null,
                -1L
        );
    }

    private ArgumentMatcher<Text2ImageParams> matchesPrompt(String expectedPrompt) {
        return params -> params.getGenerateParams().getQuery().equals(expectedPrompt);
    }
}
