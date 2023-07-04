package connected_component;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;

import java.util.Set;

public class VoteHaltMsg implements Message {
    // TODO: add necessary attributions
    Id from;
    Id to;
    Set<String> groupIds;
    boolean voteHalt;

    public VoteHaltMsg(Id from, Id to, Set<String> groupIds, boolean voteHalt) {
        this.from = from;
        this.to = to;
        this.groupIds = groupIds;
        this.voteHalt = voteHalt;}


    @Override
    public int getPriority() {
        return 0;
    }
}
