package bsplib;

import java.util.Iterator;

public abstract class Vertex<VertexValue, EdgeValue, MessageValue> {
    protected final String vertexId;
    protected VertexValue vertexValue;

    protected long superstep;

    public Vertex(String vertexId) {
        this.vertexId = vertexId;
        this.superstep = 0;
    }

//    public Vertex() {
//        // if vertexId is not specified, generate a random one
//        this(Util.getUUID());
//    }

    public abstract void compute(Iterator<MessageValue> msgIterator);
    public VertexValue getVertexValue() {
        return vertexValue;
    }

    public void setVertexValue(VertexValue v) {
        this.vertexValue = v;
    }

    public String getVertexId() {
        return vertexId;
    }

    public long getSuperstep() {
        return superstep;
    }

//    OutEdgeIterator GetOutEdgeIterator();
//    void SendMessageTo(const string& dest_vertex)

}
