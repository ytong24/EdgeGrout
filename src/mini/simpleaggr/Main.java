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

        // start a thread to check who is the root and to start/cancel a publishTask
        for(SimpleAggrScribeClient app : this.apps) {
            Thread thread = new Thread(){
                public void run(){
                    try {
                        app.checkIsRoot();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            thread.start();
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
        // BootAddr BootPort BindPort NumNode
        // Loads pastry settings
        Environment env = new Environment();
        // disable the UPnP setting (in case you are testing this on a NATted LAN)
        env.getParameters().setString("nat_search_policy", "never");


//        int BINDPORT = 9001;
//        int BOOTPORT = 9001;
        try {

            InetAddress bootaddr = InetAddress.getByName(args[0]);
            int BOOTPORT = Integer.parseInt(args[1]);
//            int numNodes = 10;
            int BINDPORT = Integer.parseInt(args[2]);
            int numNodes = Integer.parseInt(args[3]);
//            InetAddress bootaddr = InetAddress.getLocalHost();

            InetSocketAddress bootSocketAddr = new InetSocketAddress(bootaddr, BOOTPORT);

            // launch our node!
            new Main(BINDPORT, bootSocketAddr, env, numNodes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
