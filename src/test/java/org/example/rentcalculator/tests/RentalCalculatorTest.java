package org.example.rentcalculator.tests;

import org.example.rentcalculator.model.RentalProperty;
import org.example.rentcalculator.service.RentalCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RentalCalculatorTest {
    @Test
    void testPaybackCalculation() {
        RentalProperty property = new RentalProperty("Москва", 6000000, 30000, 10000, 50000);
        new RentalCalculator().recalculate(property);
        assertEquals(200, property.getPaybackPeriod());
    }

    @Test
    void testROI() {
        RentalProperty property = new RentalProperty("СПб", 4000000, 20000, 5000, 50000);
        new RentalCalculator().recalculate(property);
        assertTrue(property.getRoi() > 0);
    }
}