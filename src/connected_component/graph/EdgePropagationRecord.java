package connected_component.graph;

import java.io.Serializable;

public class EdgePropagationRecord implements Serializable {
    String targetVertexId;
    String groupId;

    public EdgePropagationRecord(String targetVertexId, String groupId) {
        this.targetVertexId = targetVertexId;
        this.groupId = groupId;
    }
}
