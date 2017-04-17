package com.twilio.callforwarding.repositories;

import com.twilio.callforwarding.models.State;

public class StateRepository extends Repository<State> {
    public StateRepository() {
        super(State.class);
    }

    public State findByStateName(String name) {
        return (State) em.createQuery("select s from State s where s.name = :name")
                .setParameter("name", name)
                .getSingleResult();
    }

}
