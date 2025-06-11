package org.example.rentcalculator.tests;

import org.example.rentcalculator.model.RentalProperty;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RentalPropertyTest {

    @Test
    void testCalculateROI() {
        RentalProperty property = new RentalProperty("Москва", 6_000_000, 30_000, 10_000, 50_000);
        property.calculateROI();
        assertEquals(0.3333333333333333, property.getRoi(), 0.001);
    }

    @Test
    void testCalculatePayback() {
        RentalProperty property = new RentalProperty("Санкт-Петербург", 4_000_000, 20_000, 8_000, 40_000);
        property.calculatePayback();
        assertEquals(336, property.getPaybackPeriod());
    }

    @Test
    void testZeroPriceDoesNotCrashROI() {
        RentalProperty property = new RentalProperty("Москва", 0, 30_000, 10_000, 50_000);
        property.calculateROI();
        assertEquals(0.0, property.getRoi());
    }

    @Test
    void testNegativeRentTaxesGivesInfinitePayback() {
        RentalProperty property = new RentalProperty("Москва", 6_000_000, 5_000, 10_000, 50_000);
        property.calculatePayback();
        assertEquals(Integer.MAX_VALUE, property.getPaybackPeriod());
    }

    @Test
    void testSettersUpdateCalculations() {
        RentalProperty property = new RentalProperty("Москва", 6_000_000, 30_000, 10_000, 50_000);

        property.setPrice(5_000_000);
        property.setRent(25_000);
        property.setTaxes(5_000);
        property.setRepairCost(30_000);

        property.calculateROI();
        property.calculatePayback();

        assertEquals(0.4, property.getRoi(), 0.001);
        assertEquals(251, property.getPaybackPeriod());
    }
}
