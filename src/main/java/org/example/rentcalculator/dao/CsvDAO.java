package org.example.rentcalculator.dao;

import org.example.rentcalculator.model.RentalProperty;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * DAO для экспорта данных в формате CSV
 */
public class CsvDAO {

    /**
     * Экспортирует список объектов в CSV-файл
     *
     * @param properties Список объектов недвижимости
     * @param filename   Имя файла для сохранения
     */
    public void exportToCSV(List<RentalProperty> properties, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Заголовки
            writer.write("Район,Цена,Аренда,Налоги,Ремонт,ROI (%),Окупаемость (мес.)\n");

            // Данные
            for (RentalProperty p : properties) {
                writer.write(String.format("%s,%.2f,%.2f,%.2f,%.2f,%.2f,%d\n",
                        p.getRegion(),
                        p.getPrice(),
                        p.getRent(),
                        p.getTaxes(),
                        p.getRepairCost(),
                        p.getRoi(),
                        p.getPaybackPeriod()));
            }

            System.out.println("Данные успешно экспортированы в " + filename);

        } catch (IOException e) {
            System.err.println("Ошибка при записи CSV-файла: " + e.getMessage());
        }
    }
}