package com.manning.neo4jia.chapter09.simple.indexbasedtrs.domain;

/**
 * Represents a phoneNumber where we do not provide a Spring Converter
 */
public class PhoneNumberWithoutConverter {

    private String number;

    public PhoneNumberWithoutConverter() {
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

}
