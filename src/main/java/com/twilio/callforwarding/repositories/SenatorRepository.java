package com.twilio.callforwarding.repositories;

import com.twilio.callforwarding.models.Senator;

public class SenatorRepository extends Repository<Senator> {

    public SenatorRepository() {
        super(Senator.class);
    }
}
