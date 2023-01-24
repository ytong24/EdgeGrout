package mini.tutorial;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;

public class MyMsg implements Message {
    /**
     * Where the Message came from.
     */
    Id from;
    /**
     * Where the Message is going.
     */
    Id to;

    public MyMsg(Id from, Id to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public int getPriority() {
        return Message.LOW_PRIORITY;
    }

    @Override
    public String toString() {
        return "MyMsg from "+from+" to "+to;
    }
}
