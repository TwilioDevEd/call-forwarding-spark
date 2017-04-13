package com.twilio.callforwarding.models;

import javax.persistence.*;

@Entity
@Table(name = "zipcodes")
public class Zipcode {

    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "zipcode")
    private Integer zipcode;

    @Column(name = "state")
    private String state;

    public Zipcode() {
    }

    public Zipcode(Long id, Integer zipcode, String state) {
        this.id = id;
        this.zipcode = zipcode;
        this.state = state;
    }

    public Zipcode(Integer zipcode, String state) {
        this.zipcode = zipcode;
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getZipcode() {
        return zipcode;
    }

    public void setZipcode(Integer zipcode) {
        this.zipcode = zipcode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
