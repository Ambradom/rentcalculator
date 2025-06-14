package org.example.rentcalculator.tests;

import org.example.rentcalculator.dao.CianApiDAO;
import org.example.rentcalculator.model.RentalProperty;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CianApiDAOTest {

    private final CianApiDAO cianDao = new CianApiDAO();

    @Test
    void testEmptyUrlReturnsTestData() throws IOException {
        RentalProperty property = cianDao.fetchFromCian("");
        assertNotNull(property);
        assertEquals("Москва", property.getRegion());
        assertEquals(6_000_000, property.getPrice(), 0.01);
        assertEquals(30_000, property.getRent(), 0.01);
    }

    @Test
    void testLoadFromLocalFile() throws IOException {
        RentalProperty property = cianDao.fetchFromCian("src/test-cian.json");

        assertNotNull(property);
        assertEquals("Москва", property.getRegion());
        assertEquals(6_000_000, property.getPrice(), 0.01);
        assertEquals(30_000, property.getRent(), 0.01);
    }

    @Test
    void testInvalidPathReturnsNull() throws IOException {
        RentalProperty result = cianDao.fetchFromCian("invalid/path.json");
        assertNull(result);
    }
}