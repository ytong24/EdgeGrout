package mini.simpleaggr;

import rice.environment.time.TimeSource;
import rice.environment.time.simple.SimpleTimeSource;
import rice.p2p.commonapi.*;
import rice.p2p.scribe.*;
import rice.pastry.PastryNode;
import rice.pastry.commonapi.PastryIdFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleAggrScribeClient implements ScribeMultiClient, Application {

    protected final Endpoint endpoint;
    private final Scribe scribe;
    private final Topic topic;
    private final Id id;
    private final String LOG_PREFIX;
    private CancellableTask publishTask;
    private SimpleAggrTreeNode treeNode;

    // how many times does root publish
    private long publishRound;

    // use it to cancel publish task when I'm not root anymore
    private Boolean isRoot;

    private TimeSource timeSource;

    // clear outdated children is a burden task, so we don't want to do it every round.
    private final int CLEAR_EVERY_N_ROUNDS = 3;

    // record how many responses does root receive in a round
    private AtomicInteger receivedMessagesCount = new AtomicInteger(0);

    public SimpleAggrScribeClient(PastryNode node) {
        String namePrefix = "SimpleAggr";
        this.endpoint = node.buildEndpoint(this, namePrefix + "Instance");
        this.id = this.endpoint.getId();
        this.LOG_PREFIX = "Node " + this.id + ":";

        this.scribe = new ScribeImpl(node, namePrefix + "ScribeInstance");

        this.topic = new Topic(new PastryIdFactory(node.getEnvironment()), namePrefix);

        this.publishRound = 0;
        this.treeNode = new SimpleAggrTreeNode(this.id);

        this.isRoot = false;
        this.timeSource = new SimpleTimeSource();

        this.endpoint.register();
    }

    /**
     * check whether I'm the root.
     * if I become a root, I need to startPublishTask()
     * if I am not a root anymore, I need to cancelPublishTask()
     *
     * @throws InterruptedException
     */
    public void checkIsRoot() throws InterruptedException {
        // TODO: do i need to change all isRoot() to this.isRoot in this file?
        while (true) {
            synchronized (this.isRoot) {
                if (this.isRoot != isRoot()) {
                    // if role change
                    this.isRoot = isRoot();
                    if (this.isRoot) {
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
        if (message instanceof PublishContent) {
            sendMulticast();
        } else if (message instanceof SimpleAggrMsg) {
            SimpleAggrMsg msg = (SimpleAggrMsg) message;
            if (!msg.to.equals(this.endpoint.getId())) {
                // not my message
                return;
            }
            System.out.println(LOG_PREFIX + " receive " + msg.message + " from " + msg.from);
            this.receivedMessagesCount.incrementAndGet();

            if (isRoot()) {
                System.out.println(LOG_PREFIX + " I'm ROOT !! My publishRound is: " + this.publishRound + ". Receive " + this.receivedMessagesCount.get() + " responses in this round");
            }

            // update publishRound
            this.publishRound = Math.max(this.publishRound, msg.publishRound);

            synchronized (this.treeNode) {
                if (this.treeNode.updateChild(msg.treeNode, this.publishRound)) {
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
        System.out.println(LOG_PREFIX + " broadcasting...");
        SimpleAggrScribeContent scribeMessages = new SimpleAggrScribeContent(this.endpoint.getLocalNodeHandle(), this.publishRound + 1);
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
        if (content instanceof SimpleAggrScribeContent) {
            SimpleAggrScribeContent req = (SimpleAggrScribeContent) content;
            System.out.println(LOG_PREFIX + " receive scribe message from " + req.from);

            if (req.from.equals(this.endpoint.getLocalNodeHandle())) {
                ++this.publishRound;
                this.receivedMessagesCount.set(1);
            }

            // clear children
            if(this.publishRound % this.CLEAR_EVERY_N_ROUNDS == 0) {
                List<Id> outdatedChildrenList = this.treeNode.clearOutdatedChildren(this.publishRound);
                if(outdatedChildrenList.isEmpty()) {
                    System.out.println(LOG_PREFIX + " clear outdated children -> no outdated children.");
                } else {
                    System.out.println(LOG_PREFIX + " clear outdated children -> " + Arrays.toString(outdatedChildrenList.toArray()));
                    System.out.println(LOG_PREFIX + " after clearing outdated children. Print tree:");
                    this.treeNode.printTreeAsRoot();
                }
            }

            // update publishRound according to content
            // TODO: whether we need to synchronize publishRound?
            this.publishRound = Math.max(req.publishRound, this.publishRound);

            synchronized (this.treeNode) {
                // tell my parent node my NodeId and my children's NodeId
                // check whether my parent is changed
                if (this.getParent() != null &&
                        (this.treeNode.getParent() == null ||
                                !this.treeNode.getParent().equals(this.getParent().getId()))) {
                    this.treeNode.setParent(this.getParent().getId());
                } else if (this.getParent() == null && this.treeNode.getParent() != null) {
                    this.treeNode.setParent(null);
                    System.out.println(LOG_PREFIX + " become new root. Print tree:");
                    this.treeNode.printTreeAsRoot();
                }

                if (this.treeNode.getParent() == null) {
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

    class PublishContent implements Message {

        @Override
        public int getPriority() {
            return MAX_PRIORITY;
        }
    }

}
