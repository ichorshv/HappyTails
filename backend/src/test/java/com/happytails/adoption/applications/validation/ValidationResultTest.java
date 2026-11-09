package com.happytails.adoption.applications.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ValidationResultTest {

    @Test
    void passMeansNoError() {
        ValidationResult result = ValidationResult.pass();

        assertTrue(result.passed());
        assertNull(result.code());
        assertNull(result.message());
    }

    @Test
    void failKeepsCodeAndExplanation() {
        ValidationResult result = ValidationResult.fail("REQUIREMENT_NOT_MET", "Животното има нужда от двор.");

        assertFalse(result.passed());
        assertEquals("REQUIREMENT_NOT_MET", result.code());
        assertEquals("Животното има нужда от двор.", result.message());


    }
}
