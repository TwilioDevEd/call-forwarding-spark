package com.twilio.callforwarding.repositories;

import com.twilio.callforwarding.models.State;

public class StateRepository extends Repository<State> {
    public StateRepository() {
        super(State.class);
    }
}
