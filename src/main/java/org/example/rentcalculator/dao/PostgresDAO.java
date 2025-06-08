package org.example.rentcalculator.dao;

import org.example.rentcalculator.model.RentalProperty;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresDAO implements RentalDAO {
    private Connection connection;

    public PostgresDAO() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/rent_calculator",
                    "postgres", "password");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(RentalProperty property) {
        String sql = "INSERT INTO properties(region, price, rent, taxes, repair_cost, roi, payback_period) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, property.getRegion());
            pstmt.setDouble(2, property.getPrice());
            pstmt.setDouble(3, property.getRent());
            pstmt.setDouble(4, property.getTaxes());
            pstmt.setDouble(5, property.getRepairCost());
            pstmt.setDouble(6, property.getRoi());
            pstmt.setInt(7, property.getPaybackPeriod());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<RentalProperty> getAll() {
        List<RentalProperty> list = new ArrayList<>();
        String sql = "SELECT * FROM properties";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                RentalProperty p = new RentalProperty();
                p.setId(rs.getInt("id"));
                p.setRegion(rs.getString("region"));
                p.setPrice(rs.getDouble("price"));
                p.setRent(rs.getDouble("rent"));
                p.setTaxes(rs.getDouble("taxes"));
                p.setRepairCost(rs.getDouble("repair_cost"));
                p.setRoi(rs.getDouble("roi"));
                p.setPaybackPeriod(rs.getInt("payback_period"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void update(RentalProperty property) {
        String sql = "UPDATE properties SET region=?, price=?, rent=?, taxes=?, repair_cost=?, roi=?, payback_period=? WHERE id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, property.getRegion());
            pstmt.setDouble(2, property.getPrice());
            pstmt.setDouble(3, property.getRent());
            pstmt.setDouble(4, property.getTaxes());
            pstmt.setDouble(5, property.getRepairCost());
            pstmt.setDouble(6, property.getRoi());
            pstmt.setInt(7, property.getPaybackPeriod());
            pstmt.setInt(8, property.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM properties WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}