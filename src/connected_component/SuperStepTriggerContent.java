package connected_component;

import rice.p2p.commonapi.Id;
import rice.p2p.scribe.ScribeContent;

public class SuperStepTriggerContent implements ScribeContent {

    protected Id from;

    protected long superstepRound;

    public SuperStepTriggerContent(Id from) {
        this(from, -1);
    }

    public SuperStepTriggerContent(Id from, long superstepRound) {
        this.from = from;
        this.superstepRound = superstepRound;
    }

}
