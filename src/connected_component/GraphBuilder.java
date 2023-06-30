package connected_component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GraphBuilder {
    public static Map<String, Set<String>> getAdjacencyListFromFile(String inputFile) throws IOException {
        Map<String, Set<String>> graph = new HashMap<>();

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) continue;

            String[] splits = line.split("\t");
            String u = splits[0], v = splits[1];
            if (graph.get(u) == null) {
                graph.put(u, new HashSet<>());
            }
            if (graph.get(v) == null) {
                graph.put(v, new HashSet<>());
            }
            graph.get(u).add(v);
            graph.get(v).add(u);
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return graph;
    }

    public static <K, V> List<Map<K, V>> splitGraph(Map<K, V> graph, long partitionNum) {
        List<Map<K, V>> partitions = new ArrayList<>();
        long verticesNum = graph.size();
        long partitionSize = verticesNum / partitionNum;

        Iterator<Map.Entry<K, V>> iterator = graph.entrySet().iterator();
        // process 0 - n-1 partitions
        for (int i = 0; i < partitionNum - 1; i++) {
            Map<K, V> currentPartition = new HashMap<>();
            for (int j = 0; j < partitionSize; j++) {
                Map.Entry<K, V> entry = iterator.next();
                currentPartition.put(entry.getKey(), entry.getValue());
            }
            partitions.add(currentPartition);
        }

        // put the rest of entries into the last partition
        Map<K, V> lastPartition = new HashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<K, V> entry = iterator.next();
            lastPartition.put(entry.getKey(), entry.getValue());
        }
        partitions.add(lastPartition);

        return partitions;
    }

    public static void main(String[] args) {
        // test
//        boolean pass1 = testGetAdjacencyListFromFile();
//        if(pass1) System.out.println("Pass 1");
//        else System.out.println("Fail 1");

        boolean pass2 = testSplitGraph();
        if(pass2) System.out.println("Pass 2");
        else System.out.println("Fail 2");

    }

    private static boolean testSplitGraph() {
        String inputFile = "inputs/soc-LiveJournal1-trim-100.txt";
        Map<String, Set<String>> graph = null;
        try {
            graph = getAdjacencyListFromFile(inputFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        List<Map<String, Set<String>>> partitions = splitGraph(graph, 5);
        System.out.printf("Partition size: %d\n", partitions.size());
        if(partitions.size() != 5) return false;

        for(int i = 0; i < partitions.size(); i++) {
            System.out.println("Partition "+i+":");
            printGraph(partitions.get(i));
            System.out.println();
        }
        return true;
    }

    private static boolean testGetAdjacencyListFromFile() {
        String inputFile = "inputs/soc-LiveJournal1-trim-100.txt";
        Map<String, Set<String>> graph = null;
        try {
            graph = getAdjacencyListFromFile(inputFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        printGraph(graph);

        System.out.printf("# Nodes: %d\n", graph.size());
        return graph.size() == 100;
    }

    private static <K,V extends Iterable<String>> void printGraph(Map<K, V> graph) {
        for (Map.Entry<K, V> entry : graph.entrySet()) {
            String key = (String) entry.getKey();
            V values = entry.getValue();
            System.out.printf("Key: %s; ", key);
            System.out.print("Values: ");
            for (String value : values) {
                System.out.print(value+" ");
            }
            System.out.println();
        }
    }
}
