package mini.tutorial;

import rice.environment.Environment;
import rice.p2p.commonapi.*;

public class MyApp implements Application {
    protected Endpoint endpoint;
    protected Node node;
    CancellableTask messageToSelfTask;
    Environment env;
    /**
     * NOTE: It is unsafe to send this message to
     * anyone but the localnode.
     *
     * The reason:
     * This is an inner class of MyApp, and therefore
     * has an automatic reference to
     * MyApp which is not serializable!  Do not
     * attempt to send over the wire, you will get a
     * NotSerializableException!
     */
    class MessageToSelf implements Message {

        @Override
        public int getPriority() {
            return MAX_PRIORITY;
        }
    }


    public MyApp(Node node) {
        // We are only going to use one instance of this application on each PastryNode
        this.endpoint = node.buildEndpoint(this, "myinstance");
        this.node = node;
        this.env = node.getEnvironment();
        // the rest of the initialization code could go here
        // TODO:

        // now we can receive messages
        this.endpoint.register();

        // Send MessageToSelf every 5 seconds, starting in 3 seconds
        this.messageToSelfTask = endpoint.scheduleMessage(new MessageToSelf(), 3000, 5000);
    }

    /**
     * Called to route a message to the id
     */
    public void routeMyMsg(Id id) {
        System.out.println(this+" sending to "+id);
        Message msg = new MyMsg(this.endpoint.getId(), id);
        this.endpoint.route(id, msg, null);
    }

    /**
     * Called to directly send a message to the nh
     */
    public void routeMyMsgDirect(NodeHandle nh) {
        System.out.println(this+" sending direct to "+nh);
        Message msg = new MyMsg(endpoint.getId(), nh.getId());
        endpoint.route(null, msg, nh);
    }

    @Override
    public boolean forward(RouteMessage message) {
        return true;
    }

    /**
     * Called when we receive a message.
     */
    @Override
    public void deliver(Id id, Message message) {
        System.out.println(this+" received "+message);
        if(message instanceof MessageToSelf) {
            // This will get called every 5 seconds, on Pastry's thread.
            // Thus now we can assume we are on Pastry's thread.
            // TODO: whatever... send messages to other nodes? print out status?
            System.out.println("I got the MessageToSelf at time:"+env.getTimeSource().currentTimeMillis());
        }

    }

    @Override
    public void update(NodeHandle handle, boolean joined) {

    }

    public String toString() {
        return "MyApp "+endpoint.getId();
    }

    public Node getNode() {
        return node;
    }

    public void cancelTask() {
        messageToSelfTask.cancel();
    }
}
