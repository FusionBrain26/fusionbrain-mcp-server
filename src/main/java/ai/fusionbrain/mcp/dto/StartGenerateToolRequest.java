package ai.fusionbrain.mcp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartGenerateToolRequest {
    private String style;
    private int width = 1024;
    private int height = 1024;
    private String prompt;
}
