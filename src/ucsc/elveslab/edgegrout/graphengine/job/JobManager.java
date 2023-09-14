package ucsc.elveslab.edgegrout.graphengine.job;


import rice.p2p.scribe.Topic;
import ucsc.elveslab.edgegrout.graphengine.GraphEngineNode;

import java.util.concurrent.ConcurrentHashMap;

public class JobManager {
    private static JobManager instance;
    private ConcurrentHashMap<String, Job<?>> idJobMap;
    private ConcurrentHashMap<String, Topic> idTopicMap;
    private ConcurrentHashMap<Topic, String> topicIdMap;

    private GraphEngineNode graphEngineNode;



    private JobManager() {
        this.idJobMap = new ConcurrentHashMap<>();
        this.idTopicMap = new ConcurrentHashMap<>();
        this.topicIdMap = new ConcurrentHashMap<>();
        this.graphEngineNode = GraphEngineNode.getInstance();
    }

    public static JobManager getInstance() {
        if (instance == null) {
            synchronized (JobManager.class) {
                if (instance == null) {
                    instance = new JobManager();
                }
            }
        }
        return instance;
    }

    public boolean registerJob(Job<?> job) {
        // check if the job is existed or not
        if(idJobMap.containsKey(job.getJobId())) return false;
        // create jobTopic for the new job
        Topic jobTopic = graphEngineNode.newTopic(job.getJobId());




        return true;
    }


}
