package connected_component.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VertexPropagationMessage implements Serializable {
    String vertexId;

//    Map<String, String> targetVerticesGroupIdMap;
    List<EdgePropagationRecord> edgeRecords;

    public VertexPropagationMessage(String vertexId) {
        this.vertexId = vertexId;
        edgeRecords = new ArrayList<>();
    }

    public void addInfo(String targetVertexId, String groupId) {
        edgeRecords.add(new EdgePropagationRecord(targetVertexId, groupId));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VertexId: ").append(vertexId).append(":\n");
        for(EdgePropagationRecord record : edgeRecords) {
            sb.append(String.format("target: %s -> groupId: %s\n", record.targetVertexId, record.groupId));
        }
        return sb.toString();
    }
}
;