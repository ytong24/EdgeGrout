package ucsc.elveslab.edgegrout.graphengine.graph.topology;

import ucsc.elveslab.edgegrout.graphengine.graph.Vertex;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GraphTopology {
    private List<Vertex> vertexList;
    private ConcurrentHashMap<Vertex, List<Vertex>> adjacencyList;

    public GraphTopology(String graphFilePath) {
        this.vertexList = new ArrayList<>();
        this.adjacencyList = new ConcurrentHashMap<>();
        // TODO: read input graph file to build up the topology
    }

    public List<Vertex> getNeighbors(Vertex vertex) {
        return adjacencyList.get(vertex);
    }

    public List<Vertex> getAllVertices() {
        return vertexList;
    }
}
