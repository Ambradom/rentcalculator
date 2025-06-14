package org.example.rentcalculator.dao;

import org.example.rentcalculator.model.RentalProperty;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO для работы с PostgreSQL
 */
public class PostgresDAO implements RentalDAO {
    private Connection connection;

    public PostgresDAO() {
        try {
            // Подключаемся к базе данных
            this.connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/rent_calculator",
                    "postgres", "213456"
            );

            // Создаём таблицу, если не существует
            createTableIfNotExists();

        } catch (SQLException e) {
            System.err.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }

    /**
     * Создаёт таблицу 'properties', если она не существует
     */
    public void createTableIfNotExists() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS properties (
                id SERIAL PRIMARY KEY,
                region VARCHAR(255),
                price NUMERIC,
                rent NUMERIC,
                taxes NUMERIC,
                repair_cost NUMERIC,
                roi NUMERIC,
                payback_period INT
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Таблица properties проверена/создана");
        }
    }

    /**
     * Сохраняет объект недвижимости в БД
     */
    @Override
    public void save(RentalProperty property) {
        if (connection == null) {
            System.err.println("Не удалось сохранить объект — соединение с БД не установлено");
            return;
        }
        String sql = """
        INSERT INTO properties(region, price, rent, taxes, repair_cost, roi, payback_period)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, property.getRegion());
            pstmt.setDouble(2, property.getPrice());
            pstmt.setDouble(3, property.getRent());
            pstmt.setDouble(4, property.getTaxes());
            pstmt.setDouble(5, property.getRepairCost());
            pstmt.setDouble(6, property.getRoi());
            pstmt.setInt(7, property.getPaybackPeriod());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        property.setId(rs.getInt(1)); // Устанавливаем ID из БД
                        System.out.println("Объект сохранён с ID: " + property.getId());
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при сохранении объекта в БД: " + e.getMessage());
        }
    }

    /**
     * Загружает все объекты из БД
     */
    @Override
    public List<RentalProperty> getAll() {
        List<RentalProperty> list = new ArrayList<>();

        if (connection == null) {
            System.err.println("Нет подключения к БД. Невозможно загрузить данные.");
            return list;
        }

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
            System.err.println("Ошибка при чтении данных из БД: " + e.getMessage());
        }

        return list;
    }

    /**
     * Обновляет существующую запись в БД
     */
    @Override
    public void update(RentalProperty property) {
        if (connection == null || property.getId() <= 0) {
            System.err.println("Невозможно обновить объект — ID не указан или нет подключения");
            return;
        }

        String sql = """
            UPDATE properties
            SET region = ?, price = ?, rent = ?, taxes = ?, repair_cost = ?, roi = ?, payback_period = ?
            WHERE id = ?
        """;

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
            System.err.println("Ошибка при обновлении записи: " + e.getMessage());
        }
    }

    /**
     * Удаляет запись по ID
     */
    @Override
    public void delete(int id) {
        if (connection == null) {
            System.err.println("Нет подключения к БД");
            return;
        }

        String sql = "DELETE FROM properties WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении записи: " + e.getMessage());
        }
    }

    /**
     * Проверяет, существует ли пользователь с таким логином
     */
    public boolean userExists(String login) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE login = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, login);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Регистрация нового пользователя
     */
    public void registerUser(String login, String password) throws SQLException {
        String sql = "INSERT INTO users(login, password) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, login);
            pstmt.setString(2, password); // В реальном проекте используй хэширование паролей
            pstmt.executeUpdate();
        }
    }

    /**
     * Аутентификация пользователя
     */
    public boolean authenticateUser(String login, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE login = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, login);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Если есть результат → авторизация успешна
        }
    }

    /**
     * Возвращает текущее соединение с БД
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Закрывает соединение с БД
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Соединение с БД закрыто");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
        }
    }
}