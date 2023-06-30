package connected_component.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PartitionPropagationMessage implements Serializable {
    List<VertexPropagationMessage> messages;

    public PartitionPropagationMessage() {
        messages = new ArrayList<>();
    }

    public void addMessage(VertexPropagationMessage message) {
        messages.add(message);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Partition Propagation Message:\n");
        for(VertexPropagationMessage m : messages) {
            sb.append(m.toString());
        }
        return sb.toString();
    }

    public long getRecordsNum() {
        return messages.size();
    }
}
