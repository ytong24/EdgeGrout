package ucsc.elveslab.edgegrout.graphengine;


import rice.p2p.commonapi.*;
import rice.p2p.scribe.*;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.standard.RandomNodeIdFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * The communication layer of GraphEngine.
 * We use it to communicate with other GraphEngine over network.
 */
public class GraphEngineNode implements Application, ScribeMultiClient {
    private final Scribe scribe;
    private final Endpoint endpoint;

    private final String APP_PREFIX = "EdgeGroutGraph";

    public GraphEngineNode(String bootAddress, int bindPort) {
        // create pastry node
        String[] splits = bootAddress.split(":");
        String hostName = splits[0];
        int bootPort = Integer.parseInt(splits[1]);
        InetSocketAddress boot = new InetSocketAddress(hostName, bootPort);
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
