package ucsc.elveslab.edgegrout.graphengine.graph.topology;

import ucsc.elveslab.edgegrout.graphengine.graph.Vertex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GraphTopology {
    private ConcurrentHashMap<Vertex, List<Vertex>> adjacencyList;

    public GraphTopology(String graphFilePath) {
        this.adjacencyList = new ConcurrentHashMap<>();
        // read input graph file to build up the adjacencyList
        getTopologyFromFile(graphFilePath);
    }

    public List<Vertex> getNeighbors(Vertex vertex) {
        return adjacencyList.get(vertex);
    }

    public Iterator<Vertex> getAllVertices() {
        return adjacencyList.keys().asIterator();
    }

    private void getTopologyFromFile(String graphFilePath) {
        Map<String, Vertex> id2Vertex = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(graphFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().startsWith("#")) {
                    // a line looks like: 'from to vertexValue edgeValue'
                    String[] splits = line.split(" ");
                    String fromId = splits[0];
                    String toId = splits[1];

                    id2Vertex.putIfAbsent(fromId, new Vertex(fromId));
                    id2Vertex.putIfAbsent(toId, new Vertex(toId));

                    Vertex from = id2Vertex.get(fromId);
                    Vertex to = id2Vertex.get(toId);

                    adjacencyList.putIfAbsent(from, new ArrayList<>());
                    adjacencyList.get(from).add(to);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
