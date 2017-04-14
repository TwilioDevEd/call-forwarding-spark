package com.twilio.callforwarding.models;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "states")
public class State {

    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy="state")
    private List<Senator> senators;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Senator> getSenators() {
        return senators;
    }

    public void setSenators(List<Senator> senators) {
        this.senators = senators;
    }

    public State(Long id, String name, List<Senator> senators) {

        this.id = id;
        this.name = name;
        this.senators = senators;
    }

    public State(String name) {
        this.name = name;
    }

    public State() {

    }
}
