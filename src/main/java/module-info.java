module org.example.rentcalculator {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.sql;

    opens org.example.rentcalculator.controller to javafx.fxml;

    exports org.example.rentcalculator.gui to javafx.graphics;
    exports org.example.rentcalculator.model;
    exports org.example.rentcalculator.dao;
    exports org.example.rentcalculator.controller;
    exports org.example.rentcalculator.service;
}