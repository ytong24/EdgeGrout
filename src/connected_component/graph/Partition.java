package connected_component.graph;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.scribe.Scribe;
import rice.p2p.scribe.Topic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Partition {
    private Map<String, Vertex> vertices;   // {vertexId, Vertex}
    public Partition() {
        this.vertices = new HashMap<>();
    }

    public <V extends Iterable<String>> void buildPartitionFromAdjacencyList(Map<String, V> adjacencyList) {
        for(String vertexId : adjacencyList.keySet()) {
            Vertex v = new Vertex(vertexId);
            vertices.putIfAbsent(vertexId, v);
        }
        for(Map.Entry<String, V> entry : adjacencyList.entrySet()) {
            String vertexId = entry.getKey();
            Vertex vertex = vertices.get(vertexId);
            for(String targetVertexId : entry.getValue()) {
                Vertex targetVertex = vertices.getOrDefault(targetVertexId, null);
                if(targetVertex == null) targetVertex = new Vertex(targetVertexId);
                vertex.addEdge(targetVertex);
            }
        }
    }

    public PartitionPropagationMessage getPartitionPropagationMessage() {
        PartitionPropagationMessage message = new PartitionPropagationMessage();
        for(Map.Entry<String, Vertex> entry : vertices.entrySet()) {
            Vertex v = entry.getValue();
            message.addMessage(v.getVertexPropagationMessage());
        }
        return message;
    }

//    public void propagate(Endpoint endpoint, Scribe scribe, Topic topic, long superstepRound) {
//        for(Map.Entry<String, Vertex> entry : vertices.entrySet()) {
//            Vertex v = entry.getValue();
//            v.propagate(endpoint, scribe, topic, superstepRound);
//        }
//    }


}
