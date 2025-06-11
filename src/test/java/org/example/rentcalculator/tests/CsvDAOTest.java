package org.example.rentcalculator.tests;

import org.example.rentcalculator.dao.CsvDAO;
import org.example.rentcalculator.model.RentalProperty;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CsvDAOTest {

    private final CsvDAO csvDAO = new CsvDAO();

    @Test
    void testExportToCSV() throws Exception {
        List<RentalProperty> properties = Arrays.asList(
                new RentalProperty("Москва", 6_000_000, 30_000, 10_000, 50_000),
                new RentalProperty("Санкт-Петербург", 4_500_000, 20_000, 8_000, 30_000)
        );

        String filename = "output-test.csv";
        csvDAO.exportToCSV(properties, filename);

        File file = new File(filename);
        assertTrue(file.exists(), "Файл output-test.csv должен быть создан");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            assertEquals("Район,Цена,Аренда,Налоги,Ремонт,ROI (%),Окупаемость (мес.)", header.trim());

            String line1 = reader.readLine();
            assertNotNull(line1);
            assertTrue(line1.contains("Москва") || line1.contains("Санкт-Петербург"));
        }

        file.deleteOnExit();
    }
}