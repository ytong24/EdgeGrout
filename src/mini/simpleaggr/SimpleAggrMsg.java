package mini.simpleaggr;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;

public class SimpleAggrMsg implements Message {
    /**
     * Where the Message came from.
     */
    Id from;
    /**
     * Where the Message is going.
     */
    Id to;

    /**
     * The message content
     */
    String message;

    SimpleAggrTreeNode treeNode;

    long publishRound;

    public SimpleAggrMsg(Id from, Id to, String message) {
        this(from, to, message, null);
    }

    public SimpleAggrMsg(Id from, Id to, String message, SimpleAggrTreeNode treeNode) {
        this(from, to, message, treeNode, -1);
    }

    public SimpleAggrMsg(Id from, Id to, String message, SimpleAggrTreeNode treeNode, long publishRound) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.treeNode = treeNode;
        this.publishRound = publishRound;
    }

    @Override
    public int getPriority() {
        return HIGH_PRIORITY;
    }
}
