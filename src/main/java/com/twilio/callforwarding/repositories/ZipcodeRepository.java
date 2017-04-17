package com.twilio.callforwarding.repositories;

import com.twilio.callforwarding.models.Zipcode;

public class ZipcodeRepository extends Repository<Zipcode> {
    public ZipcodeRepository() {
        super(Zipcode.class);
    }


    public Zipcode getFirstResultFilteredByZipcode(String zipcode) {
        return (Zipcode) em.createQuery("select z from Zipcode z where z.zipcode = :zipcode")
                .setParameter("zipcode", Integer.valueOf(zipcode))
                .getSingleResult();
    }
}
