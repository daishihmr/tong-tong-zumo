package jp.dev7.ourbeats.message;

import com.googlecode.actorom.Address;

public abstract class Message {

    protected Address from;

    public Message(Address from) {
        this.from = from;
    }

    public Address getFrom() {
        return from;
    }
}
