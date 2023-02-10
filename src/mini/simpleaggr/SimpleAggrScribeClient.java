package mini.simpleaggr;

import rice.environment.time.TimeSource;
import rice.environment.time.simple.SimpleTimeSource;
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
    private long publishRound;

    // use it to cancel publish task when I'm not root anymore
    private Boolean isRoot;

    private TimeSource timeSource;

    public SimpleAggrScribeClient(PastryNode node) {
        String namePrefix = "SimpleAggr";
        this.endpoint = node.buildEndpoint(this, namePrefix+"Instance");
        this.id = this.endpoint.getId();
        this.LOG_PREFIX = "Node " + this.id + ":";

        this.scribe = new ScribeImpl(node, namePrefix+"ScribeInstance");

        this.topic = new Topic(new PastryIdFactory(node.getEnvironment()), namePrefix);

        this.treeNode = new SimpleAggrTreeNode(this.id);

        this.isRoot = false;
        this.timeSource = new SimpleTimeSource();

        this.endpoint.register();
    }

    class PublishContent implements Message {

        @Override
        public int getPriority() {
            return MAX_PRIORITY;
        }
    }

    /**
     * check whether I'm the root.
     * if I become a root, I need to startPublishTask()
     * if I am not a root anymore, I need to cancelPublishTask()
     * @throws InterruptedException
     */
    public void checkIsRoot() throws InterruptedException {
        // TODO: do i need to change all isRoot() to this.isRoot in this file?
        while (true) {
            synchronized (this.isRoot) {
                if(this.isRoot != isRoot()) {
                    // if role change
                    this.isRoot = isRoot();
                    if(this.isRoot) {
                        System.out.println(LOG_PREFIX + " startPublishTask()...");
                        startPublishTask();
                    } else {
                        System.out.println(LOG_PREFIX + " cancelPublishTask()...");
                        cancelPublishTask();
                    }
                }
            }
            this.timeSource.sleep(5000);
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

            if(isRoot()) {
                System.out.println(LOG_PREFIX + " I'm ROOT !! My publishRound is: " + this.publishRound);
            }

            // update publishRound
            this.publishRound = Math.max(this.publishRound, msg.publishRound);

            synchronized (this.treeNode) {
                if (this.treeNode.updateChild(msg.treeNode)) {
                    // if an update happen
                    System.out.println(LOG_PREFIX + " update child tree node " + msg.treeNode.getMe());
                    if (isRoot()) {
                        System.out.println(LOG_PREFIX + " after " + this.publishRound + " rounds of publish. Print tree:");
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
        SimpleAggrScribeContent scribeMessages = new SimpleAggrScribeContent(this.endpoint.getLocalNodeHandle(), this.publishRound);
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
                ++this.publishRound;
                return;
            }

            // update publishRound according to content
            // TODO: whether we need to synchronize publishRound?
            this.publishRound = Math.max(req.publishRound, this.publishRound);

            synchronized (this.treeNode) {
                // tell my parent node my NodeId and my children's NodeId
                // this.getParent() might be null
                if (this.getParent() != null &&
                        (this.treeNode.getParent() == null ||
                                !this.treeNode.getParent().equals(this.getParent().getId()))) {
                    this.treeNode.setParent(this.getParent().getId());
                }

                if(this.treeNode.getParent() == null) {
                    System.out.println(LOG_PREFIX + " isRoot() == " + isRoot() + ". No need to send reply the scribe content.");
                    return;
                }

                SimpleAggrMsg message = new SimpleAggrMsg(this.id, this.treeNode.getParent(), "HI", this.treeNode, this.publishRound);
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
