package org.example.rentcalculator.model;

import java.io.Serializable;

/**
 * Модель объекта недвижимости
 */
public class RentalProperty implements Serializable {
    private int id;
    private String region;
    private double price;
    private double rent;
    private double taxes;
    private double repairCost;
    private double roi;
    private int paybackPeriod;

    public RentalProperty() {}

    public RentalProperty(String region, double price, double rent, double taxes, double repairCost) {
        this.region = region;
        this.price = price;
        this.rent = rent;
        this.taxes = taxes;
        this.repairCost = repairCost;
        calculateROI();
        calculatePayback();
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public double getPrice() { return price; }
    public void setPrice(double price) {
        this.price = price;
        calculateROI();
        calculatePayback();
    }

    public double getRent() { return rent; }
    public void setRent(double rent) {
        this.rent = rent;
        calculateROI();
        calculatePayback();
    }

    public double getTaxes() { return taxes; }
    public void setTaxes(double taxes) {
        this.taxes = taxes;
        calculateROI();
        calculatePayback();
    }

    public double getRepairCost() { return repairCost; }
    public void setRepairCost(double repairCost) {
        this.repairCost = repairCost;
        calculateROI();
        calculatePayback();
    }

    public double getRoi() { return roi; }
    public void setRoi(double roi) { this.roi = roi; }

    public int getPaybackPeriod() { return paybackPeriod; }
    public void setPaybackPeriod(int paybackPeriod) { this.paybackPeriod = paybackPeriod; }

    public void calculateROI() {
        if (price != 0) {
            this.roi = ((rent - taxes) / price) * 100;
        } else {
            this.roi = 0;
        }
    }

    public void calculatePayback() {
        if (rent > taxes) {
            this.paybackPeriod = (int) ((price + repairCost) / (rent - taxes));
        } else {
            this.paybackPeriod = Integer.MAX_VALUE;
        }
    }

    @Override
    public String toString() {
        return "RentalProperty{" +
                "id=" + id +
                ", region='" + region + '\'' +
                ", price=" + price +
                ", rent=" + rent +
                ", taxes=" + taxes +
                ", repairCost=" + repairCost +
                ", roi=" + roi +
                ", paybackPeriod=" + paybackPeriod +
                '}';
    }
}