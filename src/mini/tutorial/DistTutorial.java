package mini.tutorial;

import rice.environment.Environment;
import rice.p2p.commonapi.Id;
import rice.pastry.NodeHandle;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.leafset.LeafSet;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class DistTutorial {

    /**
     * This constructor sets up a PastryNode.  It will bootstrap to an
     * existing ring if it can find one at the specified location, otherwise
     * it will start a new ring.
     *
     * @param bindport the local port to bind to
     * @param bootaddress the IP:port of the node to boot from
     * @param env the environment for these nodes
     */
    public DistTutorial(int bindport, InetSocketAddress bootaddress, Environment env) throws Exception {
        // Generate the NodeIds Randomly
        NodeIdFactory nidFactory = new RandomNodeIdFactory(env);

        // construct the PastryNodeFactory, this is how we use rice.pastry.socket
        SocketPastryNodeFactory factory = new SocketPastryNodeFactory(nidFactory, bindport, env);

        // This will return null if we there are no node at that location
        NodeHandle bootHandle = factory.getNodeHandle(bootaddress);

        // construct a node, passing the null boothandle on the first loop will
        // cause the node to start its own ring
        PastryNode node = factory.newNode();

        // construct a new MyApp
        MyApp app = new MyApp(node);

        node.boot(bootaddress);

        // the node may require sending several messages to fully boot into the ring
        synchronized (node) {
            while (!node.isReady() && !node.joinFailed()) {
                // delay so we don't busy-wait
                node.wait(500);
            }

            // abort if can't join
            if(node.joinFailed()) {
                throw new IOException("Could not join the FreePastry ring.  Reason: "+node.joinFailedReason());
            }
        }

        System.out.println("Finished creating new node "+node);



        // wait 10 seconds
        env.getTimeSource().sleep(10000);

        // as long as we're not the first node
        if(bootHandle != null) {
            // route 10 messages
            for(int i = 0; i < 10; i++) {
                // pick a key at random
                Id randId = nidFactory.generateNodeId();

                // send to that key
                app.routeMyMsg(randId);

                // wait a sec
                env.getTimeSource().sleep(1000);
            }

            // wait 10 seconds
            env.getTimeSource().sleep(10000);

            // send directly to my leafset
            LeafSet leafSet = node.getLeafSet();

            // this is a typical loop to cover your leafset.  Note that if the leafset
            // overlaps, then duplicate nodes will be sent to twice
            for(int i = -leafSet.ccwSize(); i <= leafSet.cwSize(); i++) {
                if(i == 0) {
                    // don't send to self
                    continue;
                }
                // select the item
                NodeHandle nh = leafSet.get(i);

                // send the message directly to the node
                app.routeMyMsgDirect(nh);

                // wait a sec
                env.getTimeSource().sleep(1000);
            }
        }
    }


    public static void main(String[] args) {
        // Loads pastry settings
        Environment env = new Environment();
        // disable the UPnP setting (in case you are testing this on a NATted LAN)
        env.getParameters().setString("nat_search_policy", "never");


        int BINDPORT = 9001;
        int BINDPORT2 = 9002;
        int BOOTPORT = 9001;
        try {
            InetAddress bootaddr = InetAddress.getByName("10.4.0.13");
//            System.out.println(bootaddr.getHostName());

            InetSocketAddress bootSocketAddr = new InetSocketAddress(bootaddr, BOOTPORT);

            // launch our node!
            DistTutorial dt = new DistTutorial(BINDPORT, bootSocketAddr, env);
            DistTutorial dt2 = new DistTutorial(BINDPORT2, bootSocketAddr, env);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
