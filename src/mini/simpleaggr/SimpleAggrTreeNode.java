package mini.simpleaggr;

import rice.environment.time.TimeSource;
import rice.environment.time.simple.SimpleTimeSource;
import rice.p2p.commonapi.Id;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SimpleAggrTreeNode implements Serializable {
    private Id me;
    private Id parent;
    private Map<Id, SimpleAggrTreeNode> childrenMap;

    // timeSource is not Serializable, make it transient
    transient private TimeSource timeSource;
    // latest version of current node
    private long latestVersion;

    public SimpleAggrTreeNode(Id me) {
        this(me, null);
    }

    public SimpleAggrTreeNode(Id me, Id parent) {
        this.me = me;
        this.parent = parent;
        this.childrenMap = new HashMap<>();
        this.timeSource = new SimpleTimeSource();
        this.latestVersion = this.timeSource.currentTimeMillis();
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
    public boolean updateChild(SimpleAggrTreeNode child) {
        Id childId = child.getMe();
        // check whether the child is newer than my record
        if (this.childrenMap.containsKey(childId) &&
                this.childrenMap.get(childId).getLatestVersion() >= child.getLatestVersion()) {
            // my record is newer than child, do not update
            return false;
        }
        this.childrenMap.put(childId, child);
        // update latest version
        this.latestVersion = this.timeSource.currentTimeMillis();
        return true;
    }

    public Id getMe() {
        return me;
    }

    public Id getParent() {
        return parent;
    }

    public void setParent(Id parent) {
        this.parent = parent;
        // update latest version
        this.latestVersion = this.timeSource.currentTimeMillis();
    }

    public Map<Id, SimpleAggrTreeNode> getChildrenMap() {
        return childrenMap;
    }

    public long getLatestVersion() {
        return latestVersion;
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
//            System.out.println(child);
            recursivelyPrintChildren(childId, curNode.childrenMap.get(childId), recursionDepth + 1);
        }
    }
}
