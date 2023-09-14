package ucsc.elveslab.edgegrout.graphengine.job.message;

/**
 * Type of EGMessage.
 * If message type is publish, then it will be published through a scribe
 * If message type is p2p, then it will be sent through an endpoint.
 */
public enum EGMessageType {
    PUBLISH,
    P2P;
}
