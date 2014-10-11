package com.manning.neo4jia.chapter09.simple.indexbasedtrs.spring;

import com.manning.neo4jia.chapter09.simple.indexbasedtrs.domain.PhoneNumber;
import org.springframework.core.convert.converter.Converter;

/**
 * Converts a PhoneNumber to a String
 */
public class PhoneNumber2StrConverter implements Converter<PhoneNumber, String> {

    @Override
    public String convert(PhoneNumber s) {
        return s.getNumber();
    }
}
