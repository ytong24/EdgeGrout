package mini.simpleaggr;

import rice.p2p.commonapi.*;
import rice.p2p.scribe.*;
import rice.pastry.PastryNode;
import rice.pastry.commonapi.PastryIdFactory;

import java.util.Collection;

public class SimpleAggrScribeClient implements ScribeMultiClient, Application {

    private final Scribe scribe;
    private final Topic topic;
    protected final Endpoint endpoint;
    private final Id id;

    private CancellableTask publishTask;

    private final String LOG_PREFIX;

    private SimpleAggrTreeNode treeNode;

    // how many times does root publish
    private long publishCount;

    public SimpleAggrScribeClient(PastryNode node) {
        String namePrefix = "SimpleAggr";
        this.endpoint = node.buildEndpoint(this, namePrefix+"Instance");
        this.id = this.endpoint.getId();
        this.LOG_PREFIX = "Node " + this.id + ":";

        this.scribe = new ScribeImpl(node, namePrefix+"ScribeInstance");

        this.topic = new Topic(new PastryIdFactory(node.getEnvironment()), namePrefix);

        this.treeNode = new SimpleAggrTreeNode(this.id);

        this.endpoint.register();
    }

    class PublishContent implements Message {

        @Override
        public int getPriority() {
            return MAX_PRIORITY;
        }
    }

    public void startPublishTask() {
        this.publishTask = this.endpoint.scheduleMessage(new PublishContent(), 5000, 5000);
    }

    public void cancelPublishTask() {
        this.publishTask.cancel();
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
        if(message instanceof PublishContent) {
            sendMulticast();
        } else if(message instanceof SimpleAggrMsg) {
            SimpleAggrMsg msg = (SimpleAggrMsg) message;
            if(!msg.to.equals(this.endpoint.getId())) {
                // not my message
                return;
            }
            System.out.println(LOG_PREFIX + " receive " + msg.message +  " from " + msg.from);

            synchronized (this.treeNode) {
                if (this.treeNode.updateChild(msg.treeNode)) {
                    // if an update happen
                    System.out.println(LOG_PREFIX + " update child tree node " + msg.treeNode.getMe());
                    if (isRoot()) {
                        System.out.println(LOG_PREFIX + " after " + this.publishCount + " rounds of publish:");
                        this.treeNode.printTreeAsRoot();
                    }
                } else {
                    System.out.println(LOG_PREFIX + " NO need to update child tree node " + msg.treeNode.getMe());
                }
            }
        }
    }

    private void sendMulticast() {
        System.out.println(LOG_PREFIX +" broadcasting...");
        SimpleAggrScribeContent scribeMessages = new SimpleAggrScribeContent(this.endpoint.getLocalNodeHandle());
        this.scribe.publish(this.topic, scribeMessages);
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
        if(content instanceof SimpleAggrScribeContent) {
            SimpleAggrScribeContent req = (SimpleAggrScribeContent) content;
            System.out.println(LOG_PREFIX +" receive scribe message from " + req.from);

            if(req.from.equals(this.endpoint.getLocalNodeHandle())) {
                // DO NOT send to myself
                ++this.publishCount;
                return;
            }

            synchronized (this.treeNode) {
                // tell my parent node my NodeId and my children's NodeId
                // TODO: this.getParent() might be null. Need to solve it.
                if (this.treeNode.getParent() == null || !this.treeNode.getParent().equals(this.getParent().getId())) {
                    this.treeNode.setParent(this.getParent().getId());
                }

                // set children
                for (NodeHandle childHandle : this.getChildren()) {
                    this.treeNode.addNewChild(childHandle.getId());
                }
                SimpleAggrMsg message = new SimpleAggrMsg(this.id, this.treeNode.getParent(), "HI", this.treeNode);
                this.endpoint.route(this.treeNode.getParent(), message, null);
            }
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

}
