package connected_component;

import connected_component.graph.Partition;
import connected_component.graph.PartitionPropagationMessage;
import rice.p2p.commonapi.*;
import rice.p2p.scribe.*;
import rice.pastry.PastryNode;
import rice.pastry.commonapi.PastryIdFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectedComponentNode implements ScribeMultiClient, Application {

    protected final Endpoint endpoint;
    private final Scribe scribe;
    private final Topic topic;
    private final Id id;
    private final String LOG_PREFIX;

    // how many times does root publish
    private long superstepRound;

    // the partition in this node
    private Partition graphPartition;

    // record how many responses does root receive in a round
    private AtomicInteger testReceivedMessagesCount = new AtomicInteger(0);

    public ConnectedComponentNode(PastryNode node) {
        String namePrefix = "ConnectedComponent";
        this.endpoint = node.buildEndpoint(this, namePrefix + "Instance");
        this.id = this.endpoint.getId();
        this.LOG_PREFIX = "Node " + this.id + ":";

        this.scribe = new ScribeImpl(node, namePrefix + "ScribeInstance");

        this.topic = new Topic(new PastryIdFactory(node.getEnvironment()), namePrefix);

        this.superstepRound = 0;

        this.graphPartition = new Partition();

        this.endpoint.register();
    }

    /**
     * Build a part of the graph according to the adjacency list
     * @param adjacencyList the part of graph that assign to this worker
     */
    public void buildGraphPartition(Map<String, Set<String>> adjacencyList) {
        // create an efficient data structure to store topology information and node handlers of neighbor vertices
        graphPartition.buildPartitionFromAdjacencyList(adjacencyList);
    }

    /**
     * Start super steps until all vertices vote for halt
     */
    public void startEngine() {
//        while(true) {
            // TODO: trigger next super step
            triggerNextSuperStep();
            // TODO: if all vertices vote for halt, break;
//        }
    }

    public void triggerNextSuperStep() {
        // TODO: use this.scribe to publish super step content
        System.out.println(LOG_PREFIX + " trigger next super step...");
        SuperStepTriggerContent scribeMessages = new SuperStepTriggerContent(this.endpoint.getLocalNodeHandle(), this.superstepRound + 1);
        this.scribe.publish(this.topic, scribeMessages);
    }

    public void subscribe() {
        this.scribe.subscribe(this.topic, this);
    }

    @Override
    public boolean forward(RouteMessage message) {
        return true;
    }

    @Override
    public void deliver(Id id, Message message) {
//        if (message instanceof PropagateContent) {
//            PropagateContent msg = (PropagateContent) message;
//            if (msg.to != null && !msg.to.equals(this.endpoint.getId())) {
//                // not my message
//                return;
//            }
//            System.out.println(LOG_PREFIX + " receive message from " + msg.from);
//            this.receivedMessagesCount.incrementAndGet();
//
//            if (isRoot()) {
//                System.out.println(LOG_PREFIX + " I'm ROOT !! My publishRound is: " + this.superstepRound + ". Receive " + this.receivedMessagesCount.get() + " responses in this round");
//            }
//
//            // update publishRound
//            this.superstepRound = Math.max(this.superstepRound, msg.superstepRound);
//        }
    }

    @Override
    public void update(NodeHandle handle, boolean joined) {

    }

    @Override
    public boolean anycast(Topic topic, ScribeContent content) {
        return false;
    }

    @Override
    public void deliver(Topic topic, ScribeContent content) {
        if (content instanceof SuperStepTriggerContent) {
            // TODO: propagate
            PartitionPropagationMessage propagationMessage = graphPartition.getPartitionPropagationMessage();
            PropagateContent propagatePublishContent = new PropagateContent(this.id, propagationMessage, superstepRound);
            this.scribe.publish(this.topic, propagatePublishContent);
            // TODO: get message

            // TODO: update

            // TODO: vote for halt

            return;
//            SuperStepContent req = (SuperStepContent) content;
//            System.out.println(LOG_PREFIX + " receive scribe message from " + req.from);
//
//            if (req.from.equals(this.endpoint.getLocalNodeHandle())) {
//                ++this.publishRound;
//                this.receivedMessagesCount.set(1);
//            }
//
//            // update publishRound according to content
//            this.publishRound = Math.max(req.publishRound, this.publishRound);
//
//            synchronized (this.treeNode) {
//                // tell my parent node my NodeId and my children's NodeId
//                // check whether my parent is changed
//                if (this.getParent() != null &&
//                        (this.treeNode.getParent() == null ||
//                                !this.treeNode.getParent().equals(this.getParent().getId()))) {
//                    this.treeNode.setParent(this.getParent().getId());
//                } else if (this.getParent() == null && this.treeNode.getParent() != null) {
//                    this.treeNode.setParent(null);
//                    System.out.println(LOG_PREFIX + " become new root. Print tree:");
//                    this.treeNode.printTreeAsRoot();
//                }
//
//                if (this.treeNode.getParent() == null) {
//                    System.out.println(LOG_PREFIX + " isRoot() == " + isRoot() + ". No need to send reply the scribe content.");
//                    return;
//                }
//
//                ConnectedComponentMsg message = new ConnectedComponentMsg(this.id, this.treeNode.getParent(), "HI", this.treeNode, this.publishRound);
//                this.endpoint.route(this.treeNode.getParent(), message, null);
//            }
        }

        if(content instanceof PropagateContent) {
            PropagateContent propagateContent = (PropagateContent)content;
            if(isRoot()) {
                System.out.println("Receive propagation content from: " + propagateContent.from);
                System.out.println(propagateContent.message);
                testReceivedMessagesCount.addAndGet((int) propagateContent.message.getRecordsNum());
                System.out.println("Total Vertices Number: " + testReceivedMessagesCount.get());
            }
            return;
        }
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

    /************ Some passthrough accessors for the myScribe *************/
    public boolean isRoot() {
        return this.scribe.isRoot(this.topic);
    }

    public NodeHandle getParent() {
        return this.scribe.getParent(this.topic);
    }

    public Collection<NodeHandle> getChildren() {
        return this.scribe.getChildrenOfTopic(this.topic);
    }

    static class PublishContent implements Message {

        @Override
        public int getPriority() {
            return MAX_PRIORITY;
        }
    }

}
