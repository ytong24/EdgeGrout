package connected_component;

import rice.p2p.commonapi.NodeHandle;
import rice.p2p.scribe.ScribeContent;

public class SuperStepTriggerContent implements ScribeContent {

    protected NodeHandle from;

    protected long publishRound;

    public SuperStepTriggerContent(NodeHandle from) {
        this(from, -1);
    }

    public SuperStepTriggerContent(NodeHandle from, long publishRound) {
        this.from = from;
        this.publishRound = publishRound;
    }

}
