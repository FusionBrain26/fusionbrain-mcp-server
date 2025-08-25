package ai.fusionbrain.mcp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartGenerateToolResponse {
    private GenerateRequestStatus status;
    private UUID taskId;
    private String description;
    private Long statusRequestDelay;

    public static StartGenerateToolResponse failed(String description) {
        return new StartGenerateToolResponse(GenerateRequestStatus.ERROR, null, description, null);
    }

    public static StartGenerateToolResponse started(UUID resourceId, Long statusRequestDelay) {
        return new StartGenerateToolResponse(GenerateRequestStatus.STARTED, resourceId, null, statusRequestDelay);
    }
}
