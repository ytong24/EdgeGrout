package mini.tutorial;

import rice.p2p.commonapi.*;
import rice.p2p.scribe.*;
import rice.pastry.PastryNode;
import rice.pastry.commonapi.PastryIdFactory;

import java.util.Collection;

/**
 * We implement the Application interface to receive regular timed messages (see lesson5).
 * We implement the ScribeClient interface to receive scribe messages (called ScribeContent).
 *
 */
public class MyScribeClient implements ScribeMultiClient, Application {

    /**
     * My handle to a scribe impl.
     */
    private Scribe myScribe;

    /**
     * The only topic this appl is subscribing to.
     */
    private Topic myTopic;

    /**
     * The Endpoint represents the underlieing node.  By making calls on the
     * Endpoint, it assures that the message will be delivered to a MyApp on whichever
     * node the message is intended for.
     */
    protected Endpoint endpoint;

    /**
     * This task kicks off publishing and anycasting.
     * We hold it around in case we ever want to cancel the publishTask.
     */
    CancellableTask publishTask;

    /**
     * The message sequence number.  Will be incremented after each send.
     */
    int seqNum = 0;

    public MyScribeClient(PastryNode node) {
        this.endpoint = node.buildEndpoint(this, "myinstance");

        // construct Scribe
        this.myScribe = new ScribeImpl(node, "myScribeInstance");

        // construct the topic
        this.myTopic = new Topic(new PastryIdFactory(node.getEnvironment()), "example topic");
        System.out.println("myTopic = "+myTopic);

        // now we can receive messages
        this.endpoint.register();
    }

    class PublishContent implements Message {

        @Override
        public int getPriority() {
            return MAX_PRIORITY;
        }
    }

    public void subscribe() {
        this.myScribe.subscribe(this.myTopic, this);
    }

    public void startPublishTask() {
        this.publishTask = this.endpoint.scheduleMessage(new PublishContent(), 5000, 5000);
    }

    /**
     * This method is invoked on applications when the underlying node
     * is about to forward the given message with the provided target to
     * the specified next hop.  Applications can change the contents of
     * the message, specify a different nextHop (through re-routing), or
     * completely terminate the message.
     *
     * @param message The message being sent, containing an internal message
     *                along with a destination key and nodeHandle next hop.
     * @return Whether or not to forward the message further
     */
    @Override
    public boolean forward(RouteMessage message) {
        return false;
    }

    /**
     * This method is called on the application at the destination node
     * for the given id.
     *
     * @param id      The destination id of the message
     * @param message The message being sent
     */
    @Override
    public void deliver(Id id, Message message) {
        if(message instanceof PublishContent) {
            sendMulticast();
            sendAnycast();
        }
    }

    /**
     * Sends the multicast message.
     */
    public void sendMulticast() {
        System.out.println("Node "+endpoint.getLocalNodeHandle()+" broadcasting "+seqNum);
        MyScribeContent myMessage = new MyScribeContent(this.endpoint.getLocalNodeHandle(), this.seqNum);
        this.myScribe.publish(this.myTopic, myMessage);
        ++seqNum;
    }

    /**
     * Sends an anycast message.
     */
    public void sendAnycast() {
        System.out.println("Node "+endpoint.getLocalNodeHandle()+" anycasting "+seqNum);
        MyScribeContent myMessage = new MyScribeContent(endpoint.getLocalNodeHandle(), seqNum);
        this.myScribe.anycast(this.myTopic, myMessage);
        ++seqNum;
    }

    /**
     * This method is invoked to inform the application that the given node
     * has either joined or left the neighbor set of the local node, as the set
     * would be returned by the neighborSet call.
     *
     * @param handle The handle that has joined/left
     * @param joined Whether the node has joined or left
     */
    @Override
    public void update(NodeHandle handle, boolean joined) {

    }

    /**
     * This method is invoked when an anycast is received for a topic
     * which this client is interested in.  The client should return
     * whether or not the anycast should continue.
     *
     * @param topic   The topic the message was anycasted to
     * @param content The content which was anycasted
     * @return Whether or not the anycast should continue, true if we will accept the anycast
     */
    @Override
    public boolean anycast(Topic topic, ScribeContent content) {
        boolean returnValue = this.myScribe.getEnvironment().getRandomSource().nextInt(3) == 0;
        System.out.println("MyScribeClient.anycast("+topic+","+content+"):"+returnValue);
        return returnValue;
    }

    /**
     * This method is invoked when a message is delivered for a topic this
     * client is interested in.
     *
     * @param topic   The topic the message was published to
     * @param content The content which was published
     */
    @Override
    public void deliver(Topic topic, ScribeContent content) {
        System.out.println("MyScribeClient.deliver("+topic+","+content+")");
    }

    /**
     * Informs this client that a child was added to a topic in
     * which it was interested in.
     *
     * @param topic The topic to unsubscribe from
     * @param child The child that was added
     */
    @Override
    public void childAdded(Topic topic, NodeHandle child) {

    }

    /**
     * Informs this client that a child was removed from a topic in
     * which it was interested in.
     *
     * @param topic The topic to unsubscribe from
     * @param child The child that was removed
     */
    @Override
    public void childRemoved(Topic topic, NodeHandle child) {

    }

    /**
     * Informs the client that a subscribe on the given topic failed
     * - the client should retry the subscribe or take appropriate
     * action.
     *
     * @param topic The topic which the subscribe failed on
     * @deprecated use subscribeFailed(Collection<Topic> topics)
     */
    @Override
    public void subscribeFailed(Topic topic) {

    }

    /**
     * Informs the client that a subscribe on the given topic failed
     * - the client should retry the subscribe or take appropriate
     * action.
     *
     * @param topics
     */
    @Override
    public void subscribeFailed(Collection<Topic> topics) {

    }

    /**
     * Informs the client that a subscribe on the given topic failed
     * - the client should retry the subscribe or take appropriate
     * action.
     *
     * @param topics
     */
    @Override
    public void subscribeSuccess(Collection<Topic> topics) {

    }

    /************ Some passthrough accessors for the myScribe *************/
    public boolean isRoot() {
        return this.myScribe.isRoot(this.myTopic);
    }

    public NodeHandle getParent() {
        return this.myScribe.getParent(this.myTopic);
    }

    public Collection<NodeHandle> getChildren() {
        return this.myScribe.getChildrenOfTopic(this.myTopic);
    }
}
