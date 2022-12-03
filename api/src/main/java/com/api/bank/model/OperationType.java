package com.api.bank.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;


@Entity
public class OperationType extends Base {

    @Column( nullable=false, length=25)
    private String label; // Withdraw or Credit...
}
