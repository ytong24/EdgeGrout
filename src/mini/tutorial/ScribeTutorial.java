package mini.tutorial;

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

public class ScribeTutorial {

    /**
     * this will keep track of our Scribe applications
     */
    List<MyScribeClient> apps;

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
    public ScribeTutorial(int bindport, InetSocketAddress bootaddress, Environment env, int numNodes) throws Exception {
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
            MyScribeClient app = new MyScribeClient(node);
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
        Iterator<MyScribeClient> it = this.apps.iterator();
        MyScribeClient app = it.next();
        app.subscribe();
        app.startPublishTask();
        // for all the rest just subscribe
        while (it.hasNext()) {
            app = it.next();
            app.subscribe();
        }

        // now, print the tree
        env.getTimeSource().sleep(5000);
        printTree(this.apps);

    }

    /**
     * Note that this function only works because we have global knowledge. Doing
     * this in an actual distributed environment will take some more work.
     *
     * @param apps Vector of the applicatoins.
     */
    public static void printTree(List<MyScribeClient> apps) {
        // build a hashtable of the apps, keyed by nodehandle
        Map<NodeHandle, MyScribeClient> appMap = new HashMap<>();
        for(MyScribeClient app : apps) {
            appMap.put(app.endpoint.getLocalNodeHandle(), app);
        }

        NodeHandle seed = apps.get(0).endpoint.getLocalNodeHandle();

        // get the root
        NodeHandle root = getRoot(seed, appMap);

        // print the tree from the root down
        recursivelyPrintChildren(root, 0, appMap);
    }

    /**
     * Iteratively crawl up the tree to find the root.
     */
    public static NodeHandle getRoot(NodeHandle seed, Map<NodeHandle, MyScribeClient> appMap) {
        // WHY WE NEED RECURSION? WE CAN JUST ITERATE THROUGH THE MAP
        // PS: Iterate through a map takes O(n). But it's a tree! Use getRoot will take O(logn).
        MyScribeClient app = appMap.get(seed);
        while (!app.isRoot()) {
            seed = app.getParent();
            app = appMap.get(seed);
        }
        return seed;
    }

    /**
     * Print's self, then children.
     */
    public static void recursivelyPrintChildren(NodeHandle curNode, int recursionDepth, Map<NodeHandle, MyScribeClient> appMap) {
        // print self at appropriate tab level
        StringBuilder sb = new StringBuilder();
        for(int numTabs = 0; numTabs < recursionDepth; numTabs++) {
            sb.append("\t");
        }
        sb.append(curNode.getId().toString());
        System.out.println(sb);

        MyScribeClient app = appMap.get(curNode);
        Collection<NodeHandle> children = app.getChildren();
        for(NodeHandle child : children) {
            recursivelyPrintChildren(child, recursionDepth+1, appMap);
        }
    }

    public static void main(String[] args) {
        // Loads pastry settings
        Environment env = new Environment();
        // disable the UPnP setting (in case you are testing this on a NATted LAN)
        env.getParameters().setString("nat_search_policy", "never");


        int BINDPORT = 9001;
        int BOOTPORT = 9001;
        try {
            InetAddress bootaddr = InetAddress.getLocalHost();
            InetSocketAddress bootSocketAddr = new InetSocketAddress(bootaddr, BOOTPORT);
            int numNodes = 15;
            // launch our node!
            new ScribeTutorial(BINDPORT, bootSocketAddr, env, numNodes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
