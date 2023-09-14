package ucsc.elveslab.edgegrout.graphengine.graph.property;

import ucsc.elveslab.edgegrout.graphengine.job.Job;

import java.util.concurrent.ConcurrentHashMap;

public class GraphPropertyManager {
    private ConcurrentHashMap<Job, GraphProperty> jobGraphPropertyMap;

    public GraphPropertyManager() {
        this.jobGraphPropertyMap = new ConcurrentHashMap<>();
    }

    public GraphProperty getGraphProperty(Job job) {
        return jobGraphPropertyMap.get(job);
    }


}
