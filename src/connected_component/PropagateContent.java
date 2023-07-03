package connected_component;

import connected_component.graph.PartitionPropagationMessage;
import rice.p2p.commonapi.Id;
import rice.p2p.scribe.ScribeContent;

public class PropagateContent implements ScribeContent {
    /**
     * Where the Message came from.
     */
    Id from;

    /**
     * The message content
     */
    PartitionPropagationMessage message;

    long superstepRound;

    public PropagateContent(Id from, PartitionPropagationMessage message, long superstepRound) {
        this.from = from;
        this.message = message;
        this.superstepRound = superstepRound;
    }

}
