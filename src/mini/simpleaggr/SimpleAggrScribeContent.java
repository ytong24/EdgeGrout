package mini.simpleaggr;

import rice.p2p.commonapi.NodeHandle;
import rice.p2p.scribe.ScribeContent;

public class SimpleAggrScribeContent implements ScribeContent {

    protected NodeHandle from;

    protected long publishRound;

    public SimpleAggrScribeContent(NodeHandle from) {
        this(from, -1);
    }

    public SimpleAggrScribeContent(NodeHandle from, long publishRound) {
        this.from = from;
        this.publishRound = publishRound;
    }

}
