package jp.dev7.ourbeats.message;

import jp.dev7.ourbeats.actor.NodeActor;

import com.googlecode.actorom.Address;

public class InitMessage extends Message {

    private NodeActor node;

    public InitMessage(Address from, NodeActor nodeActor) {
        super(from);
        this.node = nodeActor;
    }

    public NodeActor getNode() {
        return node;
    }
}
