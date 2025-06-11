package org.example.rentcalculator.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.rentcalculator.dao.CianApiDAO;
import org.example.rentcalculator.dao.CsvDAO;
import org.example.rentcalculator.dao.PostgresDAO;
import org.example.rentcalculator.dao.RentalDAO;
import org.example.rentcalculator.model.RentalProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    // === Поля для расчёта ===
    @FXML private TextField regionField;
    @FXML private TextField priceField;
    @FXML private TextField rentField;
    @FXML private TextField taxesField;
    @FXML private TextField repairCostField;
    @FXML private TextField cianUrlField;

    // === Поля для фильтрации ===
    @FXML private TextField regionFilter;
    @FXML private TextField priceFromFilter;
    @FXML private TextField priceToFilter;
    @FXML private TextField rentFromFilter;
    @FXML private TextField rentToFilter;
    @FXML private TextField roiFromFilter;
    @FXML private TextField roiToFilter;
    @FXML private TextField paybackFromFilter;
    @FXML private TextField paybackToFilter;

    // === Таблица ===
    @FXML private TableView<RentalProperty> tableView;
    @FXML private TableColumn<RentalProperty, String> regionCol;
    @FXML private TableColumn<RentalProperty, Double> priceCol;
    @FXML private TableColumn<RentalProperty, Double> rentCol;
    @FXML private TableColumn<RentalProperty, Double> roiCol;
    @FXML private TableColumn<RentalProperty, Integer> paybackCol;
    @FXML private Button saveEditButton;

    // === DAO и данные ===
    private final RentalDAO dao = new PostgresDAO();
    private final CianApiDAO cianDao = new CianApiDAO();
    private double parseTextField(TextField field, String fieldName) throws NumberFormatException {
        String text = field.getText();
        if (text == null || text.trim().isEmpty()) {
            throw new NumberFormatException(fieldName + " не может быть пустым");
        }
        return Double.parseDouble(text);
    }
    // Списки данных
    private final ObservableList<RentalProperty> originalProperties = FXCollections.observableArrayList();
    private final ObservableList<RentalProperty> observableProperties = FXCollections.observableArrayList();
    private RentalProperty selectedProperty;

    public void initialize() {
        // Привязка таблицы к модели данных
        regionCol.setCellValueFactory(new PropertyValueFactory<>("region"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        rentCol.setCellValueFactory(new PropertyValueFactory<>("rent"));
        roiCol.setCellValueFactory(new PropertyValueFactory<>("roi"));
        paybackCol.setCellValueFactory(new PropertyValueFactory<>("paybackPeriod"));

        tableView.setItems(observableProperties);

        // Загружаем данные из БД
        if (dao instanceof PostgresDAO pgDAO && pgDAO.getConnection() != null) {
            originalProperties.addAll(pgDAO.getAll());
            observableProperties.addAll(originalProperties);
        }
    }

    private void clearInputFields() {
        regionField.clear();
        priceField.clear();
        rentField.clear();
        taxesField.clear();
        repairCostField.clear();
    }
    /**
     * Расчёт окупаемости и ROI по данным пользователя
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
     * Добавление нового объекта
     */
    @FXML
    private void handleAdd() {
        try {
            String region = regionField.getText();
            double price = Double.parseDouble(priceField.getText());
            double rent = Double.parseDouble(rentField.getText());
            double taxes = Double.parseDouble(taxesField.getText());
            double repairCost = Double.parseDouble(repairCostField.getText());

            RentalProperty property = new RentalProperty(region, price, rent, taxes, repairCost);
            property.calculateROI();
            property.calculatePayback();

            observableProperties.add(property);
            dao.save(property); // Сохранение в БД

            System.out.println("Объект добавлен в список");
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректные числа");
        }
    }

    /**
     * Обработчик загрузки с ЦИАН
     */
    @FXML
    private void handleLoadFromCian() throws IOException {
        String url = cianUrlField.getText();
        RentalProperty property = cianDao.fetchFromCian(url);

        if (property != null) {
            observableProperties.add(property);
            originalProperties.add(property);
            dao.save(property);
            showAlert("ЦИАН", "Данные загружены: " + property.getRegion());
        } else {
            showAlert("Ошибка", "Не удалось загрузить данные с ЦИАН");
        }
    }

    /**
     * Обработчик загрузки из БД
     */
    @FXML
    private void handleLoadFromDB() {
        if (dao instanceof PostgresDAO pgDAO && pgDAO.getConnection() != null) {
            originalProperties.setAll(pgDAO.getAll());
            observableProperties.setAll(originalProperties);
            showAlert("База данных", "Данные загружены из PostgreSQL");
        } else {
            showAlert("Ошибка", "Нет подключения к базе данных");
        }
    }

    /**
     * Обработчик удаления
     */
    @FXML
    private void handleDelete() {
        RentalProperty selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите объект для удаления");
            return;
        }

        observableProperties.remove(selected);
        originalProperties.remove(selected);
        dao.delete(selected.getId());
    }

    /**
     * Экспорт в CSV
     */
    @FXML
    private void handleExportCSV() {
        CsvDAO csvDAO = new CsvDAO();
        csvDAO.exportToCSV(originalProperties, "output.csv");
        showAlert("Экспорт", "Данные экспортированы в output.csv");
    }

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

        regionField.setText(selectedProperty.getRegion());
        priceField.setText(String.valueOf(selectedProperty.getPrice()));
        rentField.setText(String.valueOf(selectedProperty.getRent()));
        taxesField.setText(String.valueOf(selectedProperty.getTaxes()));
        repairCostField.setText(String.valueOf(selectedProperty.getRepairCost()));

        saveEditButton.setVisible(true); // показываем кнопку
    }

    /**
     * Обработчик кнопки "Сохранить изменения"
     */
    @FXML
    private void handleSaveEdit() {
        if (selectedProperty == null) {
            showAlert("Ошибка", "Не выбран объект для редактирования");
            return;
        }

        try {
            String region = regionField.getText();
            double price = parseTextField(priceField, "Цена");
            double rent = parseTextField(rentField, "Аренда");
            double taxes = parseTextField(taxesField, "Налоги");
            double repairCost = parseTextField(repairCostField, "Стоимость ремонта");

            selectedProperty.setRegion(region);
            selectedProperty.setPrice(price);
            selectedProperty.setRent(rent);
            selectedProperty.setTaxes(taxes);
            selectedProperty.setRepairCost(repairCost);
            selectedProperty.calculateROI();
            selectedProperty.calculatePayback();

            dao.update(selectedProperty); // обновление в БД

            int index = observableProperties.indexOf(selectedProperty);
            observableProperties.set(index, selectedProperty);

            clearInputFields();
            saveEditButton.setVisible(false); // скрываем кнопку
            selectedProperty = null;

        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректные числа.");
        }
    }
    /**
     * Применяет фильтры к списку объектов
     */
    @FXML
    private void applyFilters() {
        List<RentalProperty> filtered = new ArrayList<>(originalProperties);

        // По району
        String regionText = regionFilter.getText();
        if (!regionText.isEmpty()) {
            filtered.removeIf(p -> !p.getRegion().toLowerCase().contains(regionText.toLowerCase()));
        }

        // По цене
        try {
            double priceFrom = Double.parseDouble(priceFromFilter.getText());
            filtered.removeIf(p -> p.getPrice() < priceFrom);
        } catch (NumberFormatException ignored) {}

        try {
            double priceTo = Double.parseDouble(priceToFilter.getText());
            filtered.removeIf(p -> p.getPrice() > priceTo);
        } catch (NumberFormatException ignored) {}

        // По аренде
        try {
            double rentFrom = Double.parseDouble(rentFromFilter.getText());
            filtered.removeIf(p -> p.getRent() < rentFrom);
        } catch (NumberFormatException ignored) {}

        try {
            double rentTo = Double.parseDouble(rentToFilter.getText());
            filtered.removeIf(p -> p.getRent() > rentTo);
        } catch (NumberFormatException ignored) {}

        // По ROI
        try {
            double roiFrom = Double.parseDouble(roiFromFilter.getText());
            filtered.removeIf(p -> p.getRoi() < roiFrom);
        } catch (NumberFormatException ignored) {}

        try {
            double roiTo = Double.parseDouble(roiToFilter.getText());
            filtered.removeIf(p -> p.getRoi() > roiTo);
        } catch (NumberFormatException ignored) {}

        // По окупаемости
        try {
            int paybackFrom = Integer.parseInt(paybackFromFilter.getText());
            filtered.removeIf(p -> p.getPaybackPeriod() < paybackFrom);
        } catch (NumberFormatException ignored) {}

        try {
            int paybackTo = Integer.parseInt(paybackToFilter.getText());
            filtered.removeIf(p -> p.getPaybackPeriod() > paybackTo);
        } catch (NumberFormatException ignored) {}

        observableProperties.setAll(filtered);
    }

    /**
     * Сбрасывает все фильтры
     */
    @FXML
    private void resetFilters() {
        regionFilter.clear();
        priceFromFilter.clear();
        priceToFilter.clear();
        rentFromFilter.clear();
        rentToFilter.clear();
        roiFromFilter.clear();
        roiToFilter.clear();
        paybackFromFilter.clear();
        paybackToFilter.clear();
        observableProperties.setAll(originalProperties);
    }

    /**
     * Вспомогательный метод — показывает Alert
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}