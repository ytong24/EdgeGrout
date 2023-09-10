package connected_component;

import connected_component.graph.Partition;
import connected_component.graph.PartitionPropagationMessage;
import rice.environment.time.simple.SimpleTimeSource;
import rice.p2p.commonapi.*;
import rice.p2p.scribe.*;
import rice.pastry.PastryNode;
import rice.pastry.commonapi.PastryIdFactory;

import java.util.*;
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
    private final Partition graphPartition;

    private final Queue<PartitionPropagationMessage> propagationMessageQueue;

    private Set<String> totalGroupIds;

    private boolean isHalted;

    // record how many responses does root receive in a round
    private final AtomicInteger testReceivedMessagesCount = new AtomicInteger(0);

    public ConnectedComponentNode(PastryNode node) {
        String namePrefix = "ConnectedComponent";
        this.endpoint = node.buildEndpoint(this, namePrefix + "Instance");
        this.id = this.endpoint.getId();
        this.LOG_PREFIX = "Node " + this.id + ":";

        this.scribe = new ScribeImpl(node, namePrefix + "ScribeInstance");

        this.topic = new Topic(new PastryIdFactory(node.getEnvironment()), namePrefix); // topic有单独的id，是对 topic name的hash

        this.superstepRound = 0;

        this.graphPartition = new Partition();

        this.propagationMessageQueue = new ArrayDeque<>();

        this.totalGroupIds = new HashSet<>();

        this.isHalted = false;

        this.endpoint.register();
    }

    /**
     * Build a part of the graph according to the adjacency list
     *
     * @param adjacencyList the part of graph that assign to this worker
     */
    public void buildGraphPartition(Map<String, Set<String>> adjacencyList) {
        // create an efficient data structure to store topology information and node handlers of neighbor vertices
        graphPartition.buildPartitionFromAdjacencyList(adjacencyList);
    }

    /**
     * Start super steps until all vertices vote for halt
     */
    public void startEngine(int partitionNum) {
        // TODO: trigger next super step
        // for a graph that is cut into n partitions, we need at most n steps to sync the graph
        // here, we use partitionNum+2 because we want to see the effect of voteForHalt
        for(int i = 0; i <= partitionNum + 2; i++) {
            triggerNextSuperStep();

            try {
                new SimpleTimeSource().sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        // TODO: if all vertices vote for halt, break;
    }

    public void triggerNextSuperStep() {
        // TODO: use this.scribe to publish super step content
        System.out.println(LOG_PREFIX + " trigger next super step " + this.superstepRound + "...");
        SuperStepTriggerContent scribeMessages = new SuperStepTriggerContent(this.id, this.superstepRound);
        this.scribe.publish(this.topic, scribeMessages);
        this.superstepRound++;
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
        if(message instanceof VoteHaltMsg) {
            VoteHaltMsg msg = (VoteHaltMsg) message;
            if(msg.to != this.id) return;

            // check whether to halt. if yes, get the group ids and add into results
            if(msg.voteHalt) {
                this.totalGroupIds.addAll(msg.groupIds);
            }

        }
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
            if(isRoot()) return;
            if(isHalted) return;
            // TODO: get message
            // TODO: update
            long updatedTimes = 0L;
            System.out.println(LOG_PREFIX + "PropagationMessageQueue size: " + propagationMessageQueue.size());
            while (!propagationMessageQueue.isEmpty()) {
                PartitionPropagationMessage message = propagationMessageQueue.poll();
                updatedTimes += graphPartition.updatePartitionByPropagationMessage(message);
            }
            if (!isRoot()) {
                System.out.println(LOG_PREFIX + "UpdatedTimes: " + updatedTimes);
            }

            // TODO: propagate
            PartitionPropagationMessage propagationMessage = graphPartition.getPartitionPropagationMessage();
            PropagateContent propagatePublishContent = new PropagateContent(this.id, propagationMessage, superstepRound);
            this.scribe.publish(this.topic, propagatePublishContent);

            // TODO: vote for halt
            Id rootId = ((SuperStepTriggerContent) content).from;
            Set<String> groupIds = graphPartition.getGroupIds();
            this.isHalted = updatedTimes == 0 && ((SuperStepTriggerContent) content).superstepRound != 0;
            VoteHaltMsg voteHaltMsg = new VoteHaltMsg(this.id, rootId, groupIds, this.isHalted);
            this.endpoint.route(rootId, voteHaltMsg, null);

            return;
        }

        if (content instanceof PropagateContent) {
            if(isRoot()) return;
            if(isHalted) return;

            PropagateContent propagateContent = (PropagateContent) content;
            propagationMessageQueue.offer(propagateContent.message);
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

    public Set<String> getTotalGroupIds() {
        return totalGroupIds;
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
