package connected_component.graph;

import rice.p2p.commonapi.Id;

import java.util.HashMap;
import java.util.Map;

public class Vertex {
    private final String vertexId;
    private String groupId;
    private Map<String, Edge> edges;

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
        for(Map.Entry<String, Edge> entry : edges.entrySet()) {
            Vertex target = entry.getValue().getTargetVertex();
            message.addInfo(target.vertexId, groupId);
        }
        return message;
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

    //    public void propagate(Endpoint endpoint, Scribe scribe, Topic topic, long superstepRound) {
//        for(Map.Entry<String, Edge> entry : edges.entrySet()) {
//            Vertex target = entry.getValue().getTargetVertex();
//            sendMessageTo(target, scribe, topic, superstepRound, endpoint);
//        }
//    }
//
//    private void sendMessageTo(Vertex target, Scribe scribe, Topic topic, long superstepRound, Endpoint endpoint) {
//        PropagateMsg message = new PropagateMsg(nodeId, target.nodeId, vertexId, target.vertexId, groupId, superstepRound);
//        if(target.nodeId == null) {
//            // if i don't know the nodeId of the target vertex, publish the propagate message.
//            scribe.publish(topic, message);
//            return;
//        }
//        endpoint.route(target.nodeId, message, null);
//    }
}
