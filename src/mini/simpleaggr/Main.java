package mini.simpleaggr;

import rice.environment.Environment;
import rice.p2p.commonapi.NodeHandle;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;

public class Main {

    /**
     * this will keep track of our Scribe applications
     */
    List<SimpleAggrScribeClient> apps;

    /**
     * Based on the rice.tutorial.lesson4.DistTutorial
     *
     * This constructor launches numNodes PastryNodes. They will bootstrap to an
     * existing ring if one exists at the specified location, otherwise it will
     * start a new ring.
     *
     * @param bindport the local port to bind to
     * @param bootaddress the IP:port of the node to boot from
     * @param numNodes the number of nodes to create in this JVM
     * @param env the Environment
     */
    public Main(int bindport, InetSocketAddress bootaddress, Environment env, int numNodes) throws Exception {
        this.apps = new ArrayList<>();
        // Generate the NodeIds Randomly
        NodeIdFactory nidFactory = new RandomNodeIdFactory(env);

        // construct the PastryNodeFactory, this is how we use rice.pastry.socket
        PastryNodeFactory factory = new SocketPastryNodeFactory(nidFactory, bindport, env);

        // loop to construct the nodes/apps

        for(int i = 0; i < numNodes; i++) {
            // construct a new node
            PastryNode node = factory.newNode();

            // construct a new scribe application
            SimpleAggrScribeClient app = new SimpleAggrScribeClient(node);
            this.apps.add(app);

            node.boot(bootaddress);

            synchronized (node) {
                while (!node.isReady() && !node.joinFailed()) {
                    // delay so we don't busy-wait
                    node.wait(500);

                    // abort if can't join
                    if(node.joinFailed()) {
                        throw new IOException("Could not join the FreePastry ring.  Reason:"+node.joinFailedReason());
                    }
                }
            }

            System.out.println("Finished creating new node: " + node);
        }

        // for the first app subscribe then start the publishtask

        for(SimpleAggrScribeClient app : this.apps) {
            app.subscribe();
        }
        // After subscribe, we need to wait for a while to let the subscribe process finish
        env.getTimeSource().sleep(5000);
//        env.getTimeSource().sleep(90000);

        // we DON'T need to startPublishTask to get a ROOT
        SimpleAggrScribeClient root = getRoot(apps);
        if(root != null) {
            System.out.println("root is " + this.apps.indexOf(root));
            root.startPublishTask();
        }
    }

    /**
     * Iteratively crawl up the tree to find the root.
     */
    public static SimpleAggrScribeClient getRoot(List<SimpleAggrScribeClient> apps) {
        // WHY WE NEED RECURSION? WE CAN JUST ITERATE THROUGH THE MAP
        // PS: Iterate through a map takes O(n). But it's a tree! Use getRoot will take O(logn).
        Map<NodeHandle, SimpleAggrScribeClient> appMap = new HashMap<>();
        for(SimpleAggrScribeClient app : apps) {
            appMap.put(app.endpoint.getLocalNodeHandle(), app);
        }

        NodeHandle seed = apps.get(0).endpoint.getLocalNodeHandle();

        // get the root
        SimpleAggrScribeClient app = appMap.get(seed);
        while (app != null && !app.isRoot()) {
            seed = app.getParent();
            app = appMap.get(seed);
        }
        return app;
    }

    public static void main(String[] args) {
        // TODO: print log to make sure scribe work as expectation
        // Loads pastry settings
        Environment env = new Environment();
        // disable the UPnP setting (in case you are testing this on a NATted LAN)
        env.getParameters().setString("nat_search_policy", "never");


//        int BINDPORT = 9001;
        int BOOTPORT = 9001;
        try {
            int BINDPORT = Integer.parseInt(args[0]);
            InetAddress bootaddr = InetAddress.getLocalHost();
//            InetAddress bootaddr = InetAddress.getByName("10.4.0.13");
            InetSocketAddress bootSocketAddr = new InetSocketAddress(bootaddr, BOOTPORT);
            int numNodes = 120;
            // launch our node!
            new Main(BINDPORT, bootSocketAddr, env, numNodes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
