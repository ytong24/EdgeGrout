package mini.simpleaggr;

import rice.p2p.commonapi.Id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleAggrTreeNode implements Serializable {
    private Id me;
    private Id parent;
    private Map<Id, SimpleAggrTreeNode> childrenMap;

    // record the latest version that an id inform me. Use it to delete outdated children.
    transient private Map<Id, Long> lastInformVersionMap;

    // latest version of current node
    private AtomicLong version;

    // if I don't hear from a child for OUTDATED_DIFF rounds, I would clear it
    transient private final long OUTDATED_DIFF = 3;

    public SimpleAggrTreeNode(Id me) {
        this(me, null, 0);
    }

    public SimpleAggrTreeNode(Id me, Id parent, long version) {
        this.me = me;
        this.parent = parent;
        this.version = new AtomicLong(version);
        this.childrenMap = new HashMap<>();
        this.lastInformVersionMap = new HashMap<>();
    }

    /**
     * update a child of current tree node
     * if the child doesn't exist before, add it into my children.
     * if the child has older version than current record, ignore it.
     * if the child has newer version than current record, update the record.
     *
     * @param child the SimpleAggrTreeNode instance of the child
     *
     * @return false if no update is committed. true if there is an update.
     */
    public boolean updateChild(SimpleAggrTreeNode child, long curPublishRound) {
        Id childId = child.getMe();
        // update the lastInformMap
        this.lastInformVersionMap.put(childId, Math.max(curPublishRound, this.lastInformVersionMap.getOrDefault(childId, (long)-1)));
        // check whether the child is newer than my record
        if (this.childrenMap.containsKey(childId) &&
                this.childrenMap.get(childId).getVersion() >= child.getVersion()) {
            // my record is newer than child, do not update
            return false;
        }
        this.childrenMap.put(childId, child);
        // update latest version
        this.version.incrementAndGet();
        return true;
    }

    /**
     * clear outdated children from my children collections
     *
     * @param curPublishRound current publish round to call this method
     * @return list of outdated children ids
     */
    public List<Id> clearOutdatedChildren(long curPublishRound) {
        List<Id> outdatedChildren = new ArrayList<>();
        for(Id childId : this.lastInformVersionMap.keySet()) {
            if(curPublishRound - this.lastInformVersionMap.get(childId) > OUTDATED_DIFF) {
                // doesn't hear from a child for many rounds
                outdatedChildren.add(childId);
            }
        }

        if(!outdatedChildren.isEmpty()) {
            for(Id outdatedChildId : outdatedChildren) {
                // update lastInformVersionMap
                this.lastInformVersionMap.remove(outdatedChildId);
                // update childrenMap
                this.childrenMap.remove(outdatedChildId);
            }
            // update version
            this.version.incrementAndGet();
        }

        return outdatedChildren;
    }

    public Id getMe() {
        return me;
    }

    public Id getParent() {
        return parent;
    }

    public void setParent(Id parent) {
        this.parent = parent;
        // update version
        this.version.incrementAndGet();
    }

    public Map<Id, SimpleAggrTreeNode> getChildrenMap() {
        return childrenMap;
    }

    public long getVersion() {
//        return version;
        return this.version.get();
    }

    /**
     * print out the tree whose root is me
     */
    public void printTreeAsRoot() {
        recursivelyPrintChildren(this.me, this, 0);
    }

    /**
     * Print's self, then children.
     */
    private void recursivelyPrintChildren(Id id, SimpleAggrTreeNode curNode, int recursionDepth) {
        // print self at appropriate tab level
        StringBuilder sb = new StringBuilder();
        for (int numTabs = 0; numTabs < recursionDepth; numTabs++) {
            sb.append("\t");
        }
        sb.append(id.toString());
        System.out.println(sb);

        if(curNode == null) {
            return;
        }

        for (Id childId : curNode.childrenMap.keySet()) {
            recursivelyPrintChildren(childId, curNode.childrenMap.get(childId), recursionDepth + 1);
        }
    }
}
