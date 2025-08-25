package ai.fusionbrain.mcp.service;

import ai.fusionbrain.mcp.dto.GenerateToolStatus;
import ai.fusionbrain.mcp.dto.StartGenerateToolRequest;
import ai.fusionbrain.mcp.dto.StartGenerateToolResponse;
import ai.fusionbrain.mcp.dto.StatusToolResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PipelineServiceStubTest {

    private PipelineServiceStub pipelineServiceStub;

    @BeforeEach
    void setUp() {
        pipelineServiceStub = new PipelineServiceStub();
    }

    @Test
    void whenStartGenerateImage__ThenTaskStartedWithId() {
        StartGenerateToolRequest request = sampleToolRequest();

        StartGenerateToolResponse response = pipelineServiceStub.startGenerateImage(request);

        assertThat(response)
                .isNotNull()
                .matches(rs -> rs.getStatus() == ai.fusionbrain.mcp.dto.GenerateRequestStatus.STARTED)
                .matches(rs -> rs.getTaskId() != null)
                .extracting(StartGenerateToolResponse::getStatusRequestDelay)
                .isEqualTo(1L);
    }

    @Test
    void whenCheckStatusBeforeDelay__ThenInProcess() throws InterruptedException {
        StartGenerateToolResponse startResponse = pipelineServiceStub.startGenerateImage(sampleToolRequest());
        UUID taskId = startResponse.getTaskId();

        Thread.sleep(500); // меньше 1 секунды

        StatusToolResponse statusResponse = pipelineServiceStub.checkStatus(taskId);

        assertThat(statusResponse)
                .isNotNull()
                .matches(rs -> rs.getStatus() == GenerateToolStatus.IN_PROCESS)
                .matches(rs -> rs.getResult() == null);
    }

    @Test
    void whenCheckStatusAfterDelay__ThenFinished() throws InterruptedException {
        StartGenerateToolResponse startResponse = pipelineServiceStub.startGenerateImage(sampleToolRequest());
        UUID taskId = startResponse.getTaskId();

        Thread.sleep(1100); // больше 1 секунды

        StatusToolResponse statusResponse = pipelineServiceStub.checkStatus(taskId);

        assertThat(statusResponse)
                .isNotNull()
                .matches(rs -> rs.getStatus() == GenerateToolStatus.FINISHED)
                .extracting(StatusToolResponse::getResult)
                .isEqualTo("something based");
    }

    @Test
    void whenCheckStatusForUnknownTask__ThenInternalError() {
        UUID unknownTaskId = UUID.randomUUID();

        StatusToolResponse statusResponse = pipelineServiceStub.checkStatus(unknownTaskId);

        assertThat(statusResponse)
                .isNotNull()
                .matches(rs -> rs.getStatus() == GenerateToolStatus.INTERNAL_ERROR);
    }

    @Test
    void whenGenerateImageSynchronously__ThenFinishedAfterDelay() {
        StartGenerateToolRequest request = sampleToolRequest();

        StatusToolResponse response = pipelineServiceStub.generateImageSynchronously(request);

        assertThat(response)
                .isNotNull()
                .matches(rs -> rs.getStatus() == GenerateToolStatus.FINISHED)
                .extracting(StatusToolResponse::getResult)
                .isEqualTo("something based");
    }

    private StartGenerateToolRequest sampleToolRequest() {
        var toolRequest = new StartGenerateToolRequest();
        toolRequest.setPrompt("Sample prompt");
        return toolRequest;
    }
}