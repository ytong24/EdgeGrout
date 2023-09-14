package ucsc.elveslab.edgegrout.graphengine.job.impl.cc;

import rice.p2p.commonapi.Message;
import ucsc.elveslab.edgegrout.graphengine.job.bsp.BspEGMessage;
import ucsc.elveslab.edgegrout.graphengine.job.bsp.BspMessageActionType;
import ucsc.elveslab.edgegrout.graphengine.job.message.EGMessage;
import ucsc.elveslab.edgegrout.graphengine.job.message.EGMessageType;

public class CCMessage extends BspEGMessage {
    public CCMessage(EGMessageType messageType, BspMessageActionType actionType) {
        super(messageType, actionType);
    }
//    public CCMessage(EGMessageType messageType) {
//        super(messageType);
//    }

    @Override
    public int getPriority() {
        return 0;
    }
}
