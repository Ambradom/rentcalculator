module org.example.rentcalculator {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;


    opens org.example.rentcalculator to javafx.fxml;
    exports org.example.rentcalculator;
}