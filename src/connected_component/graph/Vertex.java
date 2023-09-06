package connected_component.graph;

import java.util.HashMap;
import java.util.Map;

public class Vertex {
    private final String vertexId;
    private final Map<String, Edge> edges;
    private String groupId;

    public Vertex(String vertexId) {
        this.vertexId = vertexId;
        // set the groupId the same as vertexId when initializing
        this.groupId = vertexId;
        this.edges = new HashMap<>();
    }

    public void addEdge(Vertex targetVertex) {
        edges.putIfAbsent(targetVertex.vertexId, new Edge(targetVertex));
    }

    public VertexPropagationMessage getVertexPropagationMessage() {
        VertexPropagationMessage message = new VertexPropagationMessage(vertexId);
        for (Map.Entry<String, Edge> entry : edges.entrySet()) {
            Vertex target = entry.getValue().getTargetVertex();
            message.addRecord(target.vertexId, groupId);
        }
        return message;
    }

    /**
     * @param message the message that is used to update the vertex
     * @return true if i'm updated. false if nothing change
     */
    public boolean updateVertexByPropagationMessage(EdgePropagationRecord message) {
        // TODO:
        if (this.getGroupId().compareTo(message.groupId) <= 0) return false;
        System.out.printf("VertexId: %s, GroupId: %s -> %s\n", this.vertexId, this.groupId, message.groupId);
        setGroupId(message.groupId);
        return true;
    }

    public String getVertexId() {
        return vertexId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }


}
