package org.example.rentcalculator.dao;

import org.example.rentcalculator.model.RentalProperty;

import java.util.List;

public interface RentalDAO {
    void save(RentalProperty property);
    List<RentalProperty> getAll();
    void update(RentalProperty property);
    void delete(int id);
}