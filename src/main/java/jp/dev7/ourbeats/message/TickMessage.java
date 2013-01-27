package jp.dev7.ourbeats.message;

import com.googlecode.actorom.Address;

public class TickMessage extends Message {

    public TickMessage(Address from) {
        super(from);
    }

}
