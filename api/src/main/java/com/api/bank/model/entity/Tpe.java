package com.api.bank.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;


@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Tpe extends Base {
    private String macId;
    private String TokenRegister;

    public Tpe() {
        super();
    }
}