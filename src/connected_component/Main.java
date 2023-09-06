package connected_component;

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
    List<ConnectedComponentNode> apps;

    /**
     * Based on the rice.tutorial.lesson4.DistTutorial
     * <p>
     * This constructor launches numNodes PastryNodes. They will bootstrap to an
     * existing ring if one exists at the specified location, otherwise it will
     * start a new ring.
     *
     * @param bindport    the local port to bind to
     * @param bootaddress the IP:port of the node to boot from
     * @param numNodes    the number of nodes to create in this JVM
     * @param env         the Environment
     */
    public Main(int bindport, InetSocketAddress bootaddress, Environment env, int numNodes, String inputFile) throws Exception {
        this.apps = new ArrayList<>();
        // Generate the NodeIds Randomly
        NodeIdFactory nidFactory = new RandomNodeIdFactory(env);

        // construct the PastryNodeFactory, this is how we use rice.pastry.socket
        PastryNodeFactory factory = new SocketPastryNodeFactory(nidFactory, bindport, env);

        // loop to construct the nodes/apps

        for (int i = 0; i < numNodes; i++) {
            // construct a new node
            PastryNode node = factory.newNode();

            // construct a new scribe application
            ConnectedComponentNode app = new ConnectedComponentNode(node);
            this.apps.add(app);

            node.boot(bootaddress);

            synchronized (node) {
                while (!node.isReady() && !node.joinFailed()) {
                    // delay so we don't busy-wait
                    node.wait(500);

                    // abort if can't join
                    if (node.joinFailed()) {
                        throw new IOException("Could not join the FreePastry ring.  Reason:" + node.joinFailedReason());
                    }
                }
            }

            System.out.println("Finished creating new node: " + node);
        }

        for (ConnectedComponentNode app : this.apps) {
            app.subscribe();
        }

        // TODO: asynchronously read graph file, get the topology of the whole graph.
        Map<String, Set<String>> graph = GraphBuilder.getAdjacencyListFromFile(inputFile);
        List<Map<String, Set<String>>> graphPartitions = GraphBuilder.splitGraph(graph, numNodes - 1);

        // After subscribe, we need to wait for a while to let the subscribe process finish
        env.getTimeSource().sleep(5000);

        // check each app is the root or not. if it's not root, assign a part of graph to it.
        ConnectedComponentNode root = null;
        Iterator<Map<String, Set<String>>> graphPartitionIterator = graphPartitions.iterator();
        for (ConnectedComponentNode app : this.apps) {
            if (app.isRoot()) {
                root = app;
                continue;
            }
            app.buildGraphPartition(graphPartitionIterator.next());
        }

        // TODO: trigger a publish task in the root. wait for the end.
        assert root != null;
        root.startEngine(numNodes-1);

        // TODO: get the output from the root
        int groupNums = root.getTotalGroupIds().size();
        System.out.printf("Total number of connected components: %d\n", groupNums);
    }

    /**
     * Iteratively crawl up the tree to find the root.
     */
    public static ConnectedComponentNode getRoot(List<ConnectedComponentNode> apps) {
        // WHY WE NEED RECURSION? WE CAN JUST ITERATE THROUGH THE MAP
        // PS: Iterate through a map takes O(n). But it's a tree! Use getRoot will take O(logn).
        Map<NodeHandle, ConnectedComponentNode> appMap = new HashMap<>();
        for (ConnectedComponentNode app : apps) {
            appMap.put(app.endpoint.getLocalNodeHandle(), app);
        }

        NodeHandle seed = apps.get(0).endpoint.getLocalNodeHandle();

        // get the root
        ConnectedComponentNode app = appMap.get(seed);
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

        // TODO:
//        String inputFile = "inputs/soc-LiveJournal1-trim-50000.txt";
//        String inputFile = "inputs/facebook_combined.txt";
        String inputFile = "inputs/ca-GrQc.txt";
        int BINDPORT = 9001;
        int BOOTPORT = 9001;
        try {
//            int BINDPORT = Integer.parseInt(args[0]);
            int numNodes = 10;
//            int numNodes = Integer.parseInt(args[1]);
            InetAddress bootaddr = InetAddress.getLocalHost();
//            InetAddress bootaddr = InetAddress.getByName("10.4.0.13");
            InetSocketAddress bootSocketAddr = new InetSocketAddress(bootaddr, BOOTPORT);

            // launch our nodes!
            new Main(BINDPORT, bootSocketAddr, env, numNodes, inputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
