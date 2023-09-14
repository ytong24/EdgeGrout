package ucsc.elveslab.edgegrout.graphengine.job.bsp;

import ucsc.elveslab.edgegrout.graphengine.job.Job;

public abstract class BspJob<MessageT extends BspEGMessage> extends Job<MessageT> {
    protected long superStepRound;

    public BspJob(String jobId) {
        super(jobId);
        superStepRound = 0L;
    }

    @Override
    public void runJob() {

    }

    protected void runCoordinator() {
        // act as the Master in BSP model
        // TODO: 1. check the number of voteToHalt
        // TODO: 2. trigger next super step
    }

    protected void runWorker() {
        // act as a Worker in BSP model
        // after receiving TriggerNextStep Message:
        // TODO: 1. Read from receivingMessageQueue
        // TODO: 2. Apply each message to update Propery
        // TODO: 3. Construct sendingMessage
        // TODO: 4. Put it into sendingMessageQueue
        // TODO: 5. Construct voteToHaltMessage
    }
}
