package ucsc.elveslab.edgegrout;

import ucsc.elveslab.edgegrout.graphengine.GraphEngine;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));
        String bootAddress = args[1];
        int bindPort = Integer.parseInt(args[3]);
        // TODO: get inputGraphFilePath from args[]
        String graphFilePath = "./xxxx";

        GraphEngine graphEngine = GraphEngine.getInstance(bootAddress, bindPort, graphFilePath);

        graphEngine.initGraphEngine();
        graphEngine.startGraphEngine();
    }
}
