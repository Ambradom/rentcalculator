package org.example.rentcalculator.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import org.example.rentcalculator.dao.PostgresDAO;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private GridPane gridPane;
    @FXML private Button confirmRegistrationButton;

    private final PostgresDAO userDao = new PostgresDAO();

    /**
     * Обработчик кнопки "Войти"
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            showAlert("Ошибка", "Введите логин и пароль");
            return;
        }

        try {
            if (userDao.authenticateUser(login, password)) {
                openMainView();
                ((Stage) loginField.getScene().getWindow()).close();
            } else {
                showAlert("Ошибка", "Неверный логин или пароль");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Ошибка подключения к базе данных");
        }
    }

    /**
     * Обработчик кнопки "Зарегистрироваться"
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        confirmPasswordField.setVisible(true);
        confirmPasswordField.setManaged(true);

        // Показываем кнопку "Подтвердить регистрацию"
        confirmRegistrationButton.setVisible(true);
        confirmRegistrationButton.setManaged(true);

        // Проверяем, есть ли метка "Подтвердите пароль:" в GridPane
        boolean labelExists = false;
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getRowIndex(node) == 2 && node instanceof Label && ((Label) node).getText().equals("Подтвердите пароль:")) {
                labelExists = true;
                break;
            }
        }

        // Если нет — добавляем
        if (!labelExists) {
            Label confirmLabel = new Label("Подтвердите пароль:");
            gridPane.add(confirmLabel, 0, 2);
        }

        // Проверяем, не добавлено ли поле подтверждения пароля
        if (confirmPasswordField.getParent() == null) {
            gridPane.add(confirmPasswordField, 1, 2);
        } else {
            // Уже добавлено → ничего не делаем
            System.out.println("Поле подтверждения пароля уже добавлено");
        }
    }

    /**
     * Обработчик кнопки "Подтвердить регистрацию"
     */
    @FXML
    private void handleConfirmRegistration(ActionEvent event) {
        String login = loginField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (login.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Ошибка", "Заполните все поля");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Ошибка", "Пароли не совпадают");
            return;
        }

        try {
            if (userDao.userExists(login)) {
                showAlert("Ошибка", "Логин уже занят");
                return;
            }

            userDao.registerUser(login, password);
            showAlert("Успех", "Вы успешно зарегистрированы!");

            // Переходим к главному окну
            openMainView();
            closeCurrentWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось зарегистрировать пользователя");
        }
    }

    /**
     * Открытие главного окна
     */
    private void openMainView() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/main-view.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Калькулятор арендной доходности");
            stage.setScene(new Scene(root, 1000, 800));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Закрытие текущего окна
     */
    private void closeCurrentWindow() {
        ((Stage) loginField.getScene().getWindow()).close();
    }

    /**
     * Вспомогательный метод: проверяет, есть ли метка в GridPane
     */
    private boolean isLabelExists(String text, GridPane gridPane) {
        for (javafx.scene.Node node : gridPane.getChildren()) {
            if (GridPane.getRowIndex(node) == 2 && node instanceof Label && ((Label) node).getText().equals(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Показывает диалоговое окно
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}