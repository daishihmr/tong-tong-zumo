package jp.dev7.ourbeats.message;

import jp.dev7.ourbeats.actor.RefereeActor;

import com.googlecode.actorom.Address;

public class ConnectedMessage extends Message {

    private RefereeActor referee;
    private int number;

    public ConnectedMessage(Address from, RefereeActor referee, int number) {
        super(from);
        this.referee = referee;
        this.number = number;
    }

    public RefereeActor getReferee() {
        return referee;
    }

    public int getNumber() {
        return number;
    }
}
