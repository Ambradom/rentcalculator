package org.example.rentcalculator.service;

import org.example.rentcalculator.model.RentalProperty;

/**
 * Класс для выполнения бизнес-расчётов
 */
public class RentalCalculator {

    /**
     * Обновляет показатели ROI и срок окупаемости
     */
    public void recalculate(RentalProperty property) {
        property.calculateROI();
        property.calculatePayback();
    }
}