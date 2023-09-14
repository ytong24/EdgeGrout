package ucsc.elveslab.edgegrout.graphengine.job;

import rice.p2p.commonapi.Message;
import rice.p2p.scribe.Topic;
import ucsc.elveslab.edgegrout.graphengine.graph.Graph;
import ucsc.elveslab.edgegrout.graphengine.graph.property.GraphProperty;
import ucsc.elveslab.edgegrout.graphengine.graph.property.GraphPropertyManager;
import ucsc.elveslab.edgegrout.graphengine.job.message.EGMessage;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Job<MessageT extends EGMessage> {
    protected String jobId;    // the unique identifier of a job
    protected BlockingQueue<MessageT> receivingMessageQueue;
    protected BlockingQueue<MessageT> sendingMessageQueue;

    protected boolean isMaster;

    public Job(String jobId) {
        this.jobId = jobId;
        this.receivingMessageQueue = new LinkedBlockingQueue<>();
        this.sendingMessageQueue = new LinkedBlockingQueue<>();
        this.isMaster = false;
    }

    /**
     * Initialize a job before start running it.
     * Such as initialize graph property.
     */
    public abstract void initJob();

    public abstract void runJob();


    protected GraphProperty getGraphProperty() {
        return Graph.getInstance().getGraphPropertyManager().getGraphProperty(this);
    }

    public String getJobId() {
        return jobId;
    }

    public BlockingQueue<MessageT> getReceivingMessageQueue() {
        return receivingMessageQueue;
    }

    public BlockingQueue<MessageT> getSendingMessageQueue() {
        return sendingMessageQueue;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        isMaster = master;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job<?> job = (Job<?>) o;
        return Objects.equals(jobId, job.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId);
    }
}
