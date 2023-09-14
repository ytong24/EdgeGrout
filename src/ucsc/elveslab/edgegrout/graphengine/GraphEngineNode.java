package ucsc.elveslab.edgegrout.graphengine;


import rice.environment.Environment;
import rice.p2p.commonapi.*;
import rice.p2p.scribe.*;
import rice.pastry.PastryNode;
import rice.pastry.commonapi.PastryIdFactory;
import ucsc.elveslab.edgegrout.graphengine.job.Job;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * The communication layer of GraphEngine.
 * We use it to communicate with other GraphEngine over network.
 */
public class GraphEngineNode implements Application, ScribeMultiClient {
    private static GraphEngineNode instance;
    private Scribe scribe;
    private Endpoint endpoint;

    private Environment env;

    private final String APP_PREFIX = "EdgeGroutGraph";

    private GraphEngineNode() {

    }

    public static GraphEngineNode getInstance() {
        if (instance == null) {
            synchronized (GraphEngineNode.class) {
                if (instance == null) {
                    instance = new GraphEngineNode();
                }
            }
        }
        return instance;
    }

    public void init(String bootAddress, int bindPort) {
        // create pastry node
        String[] splits = bootAddress.split(":");
        String hostName = splits[0];
        int bootPort = Integer.parseInt(splits[1]);
        InetSocketAddress boot = new InetSocketAddress(hostName, bootPort);

        // Loads pastry settings
        this.env = new Environment();
        // disable the UPnP setting (in case you are testing this on a NATted LAN)
        env.getParameters().setString("nat_search_policy", "never");

        PastryNode pastryNode = getPastryNode(boot, bindPort);

        // init endpoint
        String endpointName = APP_PREFIX + "EndpointInstance";
        this.endpoint = pastryNode.buildEndpoint(this, endpointName);

        // init scribe
        String scribeName = APP_PREFIX + "ScribeInstance";
        this.scribe = new ScribeImpl(pastryNode, scribeName);

    }

    private PastryNode getPastryNode(InetSocketAddress bootaddr, int bindPort) {
        // TODO:
        return null;
    }

    public Topic newTopic(String s) {
        return new Topic(new PastryIdFactory(env), s);
    }


    /** Application Interface **/
    @Override
    public boolean forward(RouteMessage message) {
        return false;
    }

    @Override
    public void deliver(Id id, Message message) {

    }

    @Override
    public void update(NodeHandle handle, boolean joined) {

    }

    /** ScribeMultiClient Interface **/
    @Override
    public boolean anycast(Topic topic, ScribeContent content) {
        return false;
    }

    @Override
    public void deliver(Topic topic, ScribeContent content) {

    }

    @Override
    public void childAdded(Topic topic, NodeHandle child) {

    }

    @Override
    public void childRemoved(Topic topic, NodeHandle child) {

    }

    @Override
    public void subscribeFailed(Topic topic) {

    }

    @Override
    public void subscribeFailed(Collection<Topic> topics) {

    }

    @Override
    public void subscribeSuccess(Collection<Topic> topics) {

    }
}
