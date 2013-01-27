package jp.dev7.ourbeats.message;

import jp.dev7.ourbeats.actor.NodeActor;
import jp.dev7.ourbeats.actor.RefereeActor;

import com.googlecode.actorom.Address;

public class ConnectedMessage extends Message {

    private RefereeActor referee;
    private NodeActor partner;
    private int number;

    public ConnectedMessage(Address from, RefereeActor referee,
            NodeActor partner, int number) {
        super(from);
        this.referee = referee;
        this.partner = partner;
        this.number = number;
    }

    public RefereeActor getReferee() {
        return referee;
    }

    public NodeActor getPartner() {
        return partner;
    }

    public int getNumber() {
        return number;
    }
}
