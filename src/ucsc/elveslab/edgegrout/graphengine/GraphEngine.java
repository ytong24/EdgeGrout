package ucsc.elveslab.edgegrout.graphengine;

import ucsc.elveslab.edgegrout.graphengine.graph.Graph;
import ucsc.elveslab.edgegrout.graphengine.job.JobManager;


public class GraphEngine {
    private String bootAddress;
    private int bindPort;
    private String graphFilePath;

    private GraphEngineNode graphEngineNode;
    private Graph graph;
    private JobManager jobManager;


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
        this.graphEngineNode = GraphEngineNode.getInstance();
        this.graphEngineNode.init(bootAddress, bindPort);

        // initialize graph. First, we build the Topology. Then we create the PropertyManager.
        this.graph = Graph.getInstance();
        this.graph.initGraph(graphFilePath);

        // TODO: initialize JobManager.
        this.jobManager = JobManager.getInstance();

        // TODO: create jobs.

    }

    public void startGraphEngine() {
        // TODO: start jobs.
    }
}
