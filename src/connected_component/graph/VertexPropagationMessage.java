package connected_component.graph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class VertexPropagationMessage implements Serializable {
    String vertexId;

    Map<String, String> targetVerticesGroupIdMap;

    public VertexPropagationMessage(String vertexId) {
        this.vertexId = vertexId;
        this.targetVerticesGroupIdMap = new HashMap<>();
    }

    public void addInfo(String targetVertexId, String groupId) {
        targetVerticesGroupIdMap.put(targetVertexId, groupId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VertexId: ").append(vertexId).append(":\n");
        for(Map.Entry<String, String> entry : targetVerticesGroupIdMap.entrySet()) {
            sb.append(String.format("target: %s -> groupId: %s\n", entry.getKey(), entry.getValue()));
        }
        return sb.toString();
    }
}
;