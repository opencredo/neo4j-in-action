package com.manning.neo4jia.chapter09.simple.domain;

/**
 * Represents a phoneNumber
 */
public class PhoneNumber {

    private String number;

    public PhoneNumber(String number) {
       this.number = number;
    }

    public PhoneNumber() {
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

}
