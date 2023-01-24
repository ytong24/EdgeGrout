package mini.tutorial;

import rice.p2p.commonapi.*;

public class MyApp implements Application {
    protected Endpoint endpoint;

    public MyApp(Node node) {
        // We are only going to use one instance of this application on each PastryNode
        this.endpoint = node.buildEndpoint(this, "myinstance");

        // the rest of the initialization code could go here
        // TODO:

        // now we can receive messages
        this.endpoint.register();
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
    }

    @Override
    public void update(NodeHandle handle, boolean joined) {

    }

    public String toString() {
        return "MyApp "+endpoint.getId();
    }
}
