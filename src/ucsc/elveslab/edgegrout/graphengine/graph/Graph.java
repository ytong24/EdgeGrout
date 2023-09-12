package ucsc.elveslab.edgegrout.graphengine.graph;

import ucsc.elveslab.edgegrout.graphengine.graph.property.GraphPropertyManager;
import ucsc.elveslab.edgegrout.graphengine.graph.topology.GraphTopology;

public class Graph {
    private GraphPropertyManager graphPropertyManager;
    private GraphTopology graphTopology;

    /** Singleton **/
    private volatile static Graph instance;
    private Graph(String graphFilePath) {
        // TODO: fill the constructor
        this.graphTopology = new GraphTopology(graphFilePath);
        this.graphPropertyManager = new GraphPropertyManager();
    }

    public static Graph getInstance(String graphFilePath) {
        if (instance == null) {
            synchronized (Graph.class) {
                if (instance == null) {
                    instance = new Graph(graphFilePath);
                }
            }
        }
        return instance;
    }

    public GraphPropertyManager getGraphPropertyManager() {
        return graphPropertyManager;
    }

    public GraphTopology getGraphTopology() {
        return graphTopology;
    }
}
