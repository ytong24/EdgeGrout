package ucsc.elveslab.edgegrout.graphengine.graph;

import ucsc.elveslab.edgegrout.graphengine.graph.property.GraphPropertyManager;
import ucsc.elveslab.edgegrout.graphengine.graph.topology.GraphTopology;

public class Graph {
    private GraphPropertyManager graphPropertyManager;
    private GraphTopology graphTopology;

    /** Singleton **/
    private volatile static Graph instance;
    private Graph() {
    }

    public static Graph getInstance() {
        if (instance == null) {
            synchronized (Graph.class) {
                if (instance == null) {
                    instance = new Graph();
                }
            }
        }
        return instance;
    }

    public void initGraph(String graphFilePath) {
        this.graphTopology = new GraphTopology(graphFilePath);
        this.graphPropertyManager = new GraphPropertyManager();
    }

    public GraphPropertyManager getGraphPropertyManager() {
        return graphPropertyManager;
    }

    public GraphTopology getGraphTopology() {
        return graphTopology;
    }
}
