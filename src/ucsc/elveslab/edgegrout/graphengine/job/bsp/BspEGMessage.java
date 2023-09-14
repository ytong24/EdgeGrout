package ucsc.elveslab.edgegrout.graphengine.job.bsp;

import ucsc.elveslab.edgegrout.graphengine.job.message.EGMessage;
import ucsc.elveslab.edgegrout.graphengine.job.message.EGMessageType;

public class BspEGMessage extends EGMessage {
    private BspMessageActionType actionType;
    public BspEGMessage(EGMessageType messageType, BspMessageActionType actionType) {
        super(messageType);
        this.actionType = actionType;
    }


}
