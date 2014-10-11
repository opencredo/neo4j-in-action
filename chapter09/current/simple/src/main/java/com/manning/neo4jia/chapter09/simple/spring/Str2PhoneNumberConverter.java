package com.manning.neo4jia.chapter09.simple.spring;

import com.manning.neo4jia.chapter09.simple.domain.PhoneNumber;
import org.springframework.core.convert.converter.Converter;

/**
 * Converts a String to a PhoneNumber
 */
public class Str2PhoneNumberConverter implements Converter<String, PhoneNumber> {

    @Override
    public PhoneNumber convert(String s) {
        return new PhoneNumber(s);
    }
}
