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
    public void setPrice(double price) { this.price = price; }

    public double getRent() { return rent; }
    public void setRent(double rent) { this.rent = rent; }

    public double getTaxes() { return taxes; }
    public void setTaxes(double taxes) { this.taxes = taxes; }

    public double getRepairCost() { return repairCost; }
    public void setRepairCost(double repairCost) { this.repairCost = repairCost; }

    public double getRoi() { return roi; }
    public void setRoi(double roi) { this.roi = roi; }

    public int getPaybackPeriod() { return paybackPeriod; }
    public void setPaybackPeriod(int paybackPeriod) { this.paybackPeriod = paybackPeriod; }

    private void calculateROI() {
        this.roi = ((rent - taxes) / price) * 100;
    }

    private void calculatePayback() {
        this.paybackPeriod = (int) ((price + repairCost) / (rent - taxes));
    }

    @Override
    public String toString() {
        return "RentalProperty{" +
                "region='" + region + '\'' +
                ", price=" + price +
                ", roi=" + roi +
                ", paybackPeriod=" + paybackPeriod +
                '}';
    }
}