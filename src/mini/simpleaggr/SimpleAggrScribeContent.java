package mini.simpleaggr;

import rice.p2p.commonapi.NodeHandle;
import rice.p2p.scribe.ScribeContent;

public class SimpleAggrScribeContent implements ScribeContent {

    protected NodeHandle from;

    public SimpleAggrScribeContent(NodeHandle from) {
        this.from = from;
    }
}
