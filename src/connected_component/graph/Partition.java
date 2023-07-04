package connected_component.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Partition {
    private final Map<String, Vertex> vertices;   // {vertexId, Vertex}

    public Partition() {
        this.vertices = new HashMap<>();
    }

    public <V extends Iterable<String>> void buildPartitionFromAdjacencyList(Map<String, V> adjacencyList) {
        for (String vertexId : adjacencyList.keySet()) {
            Vertex v = new Vertex(vertexId);
            vertices.putIfAbsent(vertexId, v);
        }
        for (Map.Entry<String, V> entry : adjacencyList.entrySet()) {
            String vertexId = entry.getKey();
            Vertex vertex = vertices.get(vertexId);
            for (String targetVertexId : entry.getValue()) {
                Vertex targetVertex = vertices.getOrDefault(targetVertexId, null);
                if (targetVertex == null) targetVertex = new Vertex(targetVertexId);
                vertex.addEdge(targetVertex);
            }
        }
    }

    public PartitionPropagationMessage getPartitionPropagationMessage() {
        PartitionPropagationMessage message = new PartitionPropagationMessage();
        for (Map.Entry<String, Vertex> entry : vertices.entrySet()) {
            Vertex v = entry.getValue();
            message.addMessage(v.getVertexPropagationMessage());
        }
        return message;
    }

    public Set<String> getGroupIds() {
        Set<String> groupSet = new HashSet<>();
        for(Map.Entry<String, Vertex> entry : vertices.entrySet()) {
            Vertex v = entry.getValue();
            groupSet.add(v.getGroupId());
        }
        return groupSet;
    }


    /**
     * @param message the message that is used to update this partition
     * @return the number of records that are updated. we need to use it to check volt for halt or not.
     */
    public long updatePartitionByPropagationMessage(PartitionPropagationMessage message) {
        long updatedTimes = 0L;
        for (VertexPropagationMessage m : message.messages) {
            for (EdgePropagationRecord e : m.edgeRecords) {
                if (!vertices.containsKey(e.targetVertexId)) continue;
                Vertex targetVertex = vertices.get(e.targetVertexId);
                if (targetVertex.updateVertexByPropagationMessage(e)) updatedTimes++;
            }
        }
        return updatedTimes;
    }

}
