package ai.fusionbrain.mcp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatusToolResponse {
    private GenerateToolStatus status;
    private String result;

    public StatusToolResponse(GenerateToolStatus status) {
        this.status = status;
    }
}
