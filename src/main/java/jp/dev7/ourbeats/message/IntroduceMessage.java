package jp.dev7.ourbeats.message;

import com.googlecode.actorom.Address;

public class IntroduceMessage extends Message {

    private Address partner;

    public IntroduceMessage(Address from, Address partner) {
        super(from);
        this.partner = partner;
    }

    public Address getPartner() {
        return partner;
    }

}
