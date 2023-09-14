package ucsc.elveslab.edgegrout.graphengine.graph;

import java.util.Objects;

public class Vertex {
    String vertexId;    // the unique identifier of a vertex

    public Vertex(String vertexId) {
        this.vertexId = vertexId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return Objects.equals(vertexId, vertex.vertexId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertexId);
    }
}
