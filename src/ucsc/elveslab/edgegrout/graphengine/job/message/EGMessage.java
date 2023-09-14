package ucsc.elveslab.edgegrout.graphengine.job.message;

import rice.p2p.commonapi.Message;
import rice.p2p.scribe.ScribeContent;

public class EGMessage implements Message, ScribeContent {
    private EGMessageType messageType;

    public EGMessage(EGMessageType messageType) {
        this.messageType = messageType;
    }

    public EGMessageType getMessageType() {
        return messageType;
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
