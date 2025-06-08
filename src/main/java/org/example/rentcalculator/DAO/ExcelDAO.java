package org.example.rentcalculator.dao;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.rentcalculator.model.RentalProperty;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelDAO {
    public void exportToExcel(List<RentalProperty> properties, String filename) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Объекты");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Район", "Цена", "Аренда", "Налоги", "Ремонт", "ROI", "Окупаемость"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        int rowNum = 1;
        for (RentalProperty p : properties) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(p.getId());
            row.createCell(1).setCellValue(p.getRegion());
            row.createCell(2).setCellValue(p.getPrice());
            row.createCell(3).setCellValue(p.getRent());
            row.createCell(4).setCellValue(p.getTaxes());
            row.createCell(5).setCellValue(p.getRepairCost());
            row.createCell(6).setCellValue(p.getRoi());
            row.createCell(7).setCellValue(p.getPaybackPeriod());
        }

        try (FileOutputStream fos = new FileOutputStream(filename)) {
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
