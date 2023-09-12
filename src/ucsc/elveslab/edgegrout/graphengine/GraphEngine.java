package ucsc.elveslab.edgegrout.graphengine;

import ucsc.elveslab.edgegrout.graphengine.graph.Graph;


public class GraphEngine {
    private String bootAddress;
    private int bindPort;
    private String graphFilePath;

    private GraphEngineNode graphEngineNode;
    private Graph graph;


   /** Singleton **/
    private volatile static GraphEngine instance;
    private GraphEngine(String bootAddress, int bindPort, String graphFilePath) {
        this.bootAddress = bootAddress;
        this.bindPort = bindPort;
        this.graphFilePath = graphFilePath;
    }

    public static GraphEngine getInstance(String bootAddress, int bindPort, String graphFilePath) {
        if (instance == null) {
            synchronized (GraphEngine.class) {
                if (instance == null) {
                    instance = new GraphEngine(bootAddress, bindPort, graphFilePath);
                }
            }
        }
        return instance;
    }

    public void initGraphEngine() {
        // TODO:
        // initialize communication layer: graphEngineNode
        this.graphEngineNode = new GraphEngineNode(bootAddress, bindPort);

        // TODO: initialize graph. First, we build the Topology. Then we create the PropertyManager.
        this.graph = Graph.getInstance(graphFilePath);

        // TODO: initialize JobManager.

        // TODO: create jobs.
    }

    public void startGraphEngine() {
        // TODO: start jobs.
    }
}
