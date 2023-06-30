package connected_component.graph;

public class Edge {
    private Vertex targetVertex;

    public Edge(Vertex targetVertex) {
        this.targetVertex = targetVertex;
    }

    public Vertex getTargetVertex() {
        return targetVertex;
    }
}
