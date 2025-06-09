package org.example.rentcalculator.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.ObservableList;
import org.example.rentcalculator.dao.CianApiDAO;
import org.example.rentcalculator.model.RentalProperty;
import org.example.rentcalculator.dao.PostgresDAO;

import java.io.IOException;
import java.util.List;

public class MainController {

    // Поля ввода
    @FXML private TextField regionField;
    @FXML private TextField priceField;
    @FXML private TextField rentField;
    @FXML private TextField taxesField;
    @FXML private TextField repairCostField;

    // Таблица
    @FXML private TableView<RentalProperty> tableView;
    @FXML private TableColumn<RentalProperty, String> regionCol;
    @FXML private TableColumn<RentalProperty, Double> priceCol;
    @FXML private TableColumn<RentalProperty, Double> rentCol;
    @FXML private TableColumn<RentalProperty, Double> roiCol;
    @FXML private TableColumn<RentalProperty, Integer> paybackCol;

    // DAO
    private final PostgresDAO dao = new PostgresDAO();
    private final ObservableList<RentalProperty> observableProperties = FXCollections.observableArrayList();
    private final CianApiDAO cianDao = new CianApiDAO();
    /**
     * Метод инициализации — вызывается после загрузки FXML
     */
    public void initialize() {
        regionCol.setCellValueFactory(new PropertyValueFactory<>("region"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        rentCol.setCellValueFactory(new PropertyValueFactory<>("rent"));
        roiCol.setCellValueFactory(new PropertyValueFactory<>("roi"));
        paybackCol.setCellValueFactory(new PropertyValueFactory<>("paybackPeriod"));

        tableView.setItems(observableProperties);

        // Загружаем данные из БД при старте (если доступна)
        if (dao.getConnection() != null) {
            observableProperties.addAll(dao.getAll());
        }
    }

    /**
     * Обработчик кнопки "Рассчитать"
     */
    @FXML
    private void handleCalculate() {
        try {
            String region = regionField.getText();
            double price = Double.parseDouble(priceField.getText());
            double rent = Double.parseDouble(rentField.getText());
            double taxes = Double.parseDouble(taxesField.getText());
            double repairCost = Double.parseDouble(repairCostField.getText());

            RentalProperty property = new RentalProperty(region, price, rent, taxes, repairCost);
            property.calculateROI();
            property.calculatePayback();

            showAlert("Результат", "Срок окупаемости: " + property.getPaybackPeriod() +
                    " мес.\nROI: " + String.format("%.2f", property.getRoi()) + "%");

        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Пожалуйста, введите корректные числа.");
        }
    }

    /**
     * Обработчик кнопки "Добавить в список"
     */
    @FXML
    private void handleAdd() {
        try {
            String region = regionField.getText();
            double price = Double.parseDouble(priceField.getText());
            double rent = Double.parseDouble(rentField.getText());
            double taxes = Double.parseDouble(taxesField.getText());
            double repairCost = Double.parseDouble(repairCostField.getText());

            if (selectedProperty == null) {
                // Новый объект
                RentalProperty property = new RentalProperty(region, price, rent, taxes, repairCost);
                property.calculateROI();
                property.calculatePayback();
                observableProperties.add(property);
                dao.save(property);
                System.out.println("Новый объект добавлен");
            } else {
                // Редактирование существующего
                selectedProperty.setRegion(region);
                selectedProperty.setPrice(price);
                selectedProperty.setRent(rent);
                selectedProperty.setTaxes(taxes);
                selectedProperty.setRepairCost(repairCost);
                selectedProperty.calculateROI();
                selectedProperty.calculatePayback();

                observableProperties.remove(selectedProperty);
                observableProperties.add(selectedProperty);

                dao.update(selectedProperty);
                selectedProperty = null; // очищаем выбор
                System.out.println("Объект обновлён");

                regionField.clear();
                priceField.clear();
                rentField.clear();
                taxesField.clear();
                repairCostField.clear();
            }

        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Пожалуйста, введите корректные числа.");
        }
    }
    @FXML
    private void handleLoadFromDB() {
        PostgresDAO pgDAO = dao;
        if (pgDAO.getConnection() != null) {
            observableProperties.addAll(pgDAO.getAll());
            showAlert("База данных", "Данные загружены из PostgreSQL");
        } else {
            showAlert("Ошибка", "Нет подключения к базе данных");
        }
    }
    /**
     * Обработчик кнопки "Загрузить с ЦИАН"
     */
    @FXML private TextField cianUrlField;

    @FXML
    private void handleLoadFromCian() throws IOException {
        String url = cianUrlField.getText();

        RentalProperty property = cianDao.fetchFromCian(url);

        if (property != null) {
            observableProperties.add(property);
            showAlert("ЦИАН", "Данные загружены: " + property.getRegion());
        } else {
            showAlert("Ошибка", "Не удалось получить данные. Проверьте URL или попробуйте снова.");
        }
    }

    private RentalProperty selectedProperty = null; // для хранения выделенного объекта

    /**
     * Обработчик кнопки "Редактировать выделенный"
     */
    @FXML
    private void handleEdit() {
        selectedProperty = tableView.getSelectionModel().getSelectedItem();
        if (selectedProperty == null) {
            showAlert("Ошибка", "Выберите объект для редактирования");
            return;
        }

        // Заполняем поля данными из выделенной строки
        regionField.setText(selectedProperty.getRegion());
        priceField.setText(String.valueOf(selectedProperty.getPrice()));
        rentField.setText(String.valueOf(selectedProperty.getRent()));
        taxesField.setText(String.valueOf(selectedProperty.getTaxes()));
        repairCostField.setText(String.valueOf(selectedProperty.getRepairCost()));
    }
    /**
     * Обработчик кнопки "Удалить"
     */
    @FXML
    private void handleDelete() {
        // Получаем выделенный объект
        RentalProperty selected = tableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Ошибка", "Выберите объект для удаления");
            return;
        }

        // Удаление из таблицы
        observableProperties.remove(selected);

        // Удаление из БД (если доступна)
        if (dao.getConnection() != null) {
            dao.delete(selected.getId());
            System.out.println("Объект удалён из БД");
        }
    }

    /**
     * Вспомогательный метод для отображения сообщений
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}