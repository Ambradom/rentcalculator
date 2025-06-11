package org.example.rentcalculator.tests;

import org.example.rentcalculator.dao.PostgresDAO;
import org.example.rentcalculator.model.RentalProperty;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostgresDAOTest {

    private PostgresDAO dao;
    private RentalProperty testProperty;

    @BeforeEach
    void setUp() throws SQLException {
        dao = new PostgresDAO();
        dao.createTableIfNotExists(); // Убедимся, что таблица существует
        clearTable(); // Очистим перед каждым тестом

        // Создаем тестовый объект
        testProperty = new RentalProperty("Санкт-Петербург", 5_000_000, 25_000, 8_000, 30_000);
        testProperty.calculateROI();
        testProperty.calculatePayback();

        // Сохраняем в БД
        dao.save(testProperty);

        // Получаем ID последней записи
        List<RentalProperty> all = dao.getAll();
        assertNotNull(all);
        assertFalse(all.isEmpty());
        testProperty.setId(all.get(0).getId()); // ← получаем id из БД
    }

    @AfterEach
    void tearDown() throws SQLException {
        clearTable();
        dao.closeConnection();
    }

    private void clearTable() throws SQLException {
        try (var stmt = dao.getConnection().createStatement()) {
            stmt.execute("DELETE FROM properties");
        }
    }

    @Test
    void testSaveAndGet() {
        List<RentalProperty> result = dao.getAll();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Санкт-Петербург", result.get(0).getRegion());
    }

    @Test
    void testUpdate() throws SQLException {
        // GIVEN
        RentalProperty property = new RentalProperty("Санкт-Петербург", 5_000_000, 25_000, 8_000, 30_000);
        property.calculateROI();
        property.calculatePayback();

        dao.save(property); // Сохраняем
        int id = property.getId(); // Получаем ID из БД
        assertNotNull(id);

        // WHEN
        property.setRegion("Обновлённый район");
        property.setPrice(6_000_000);
        property.setRent(30_000);
        property.setTaxes(10_000);
        property.setRepairCost(50_000);
        property.calculateROI();
        property.calculatePayback();

        dao.update(property); // Обновляем в БД

        // Загружаем обновлённые данные из БД
        List<RentalProperty> updatedList = dao.getAll();
        RentalProperty updatedProp = updatedList.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);

        // THEN
        assertNotNull(updatedProp);
        assertEquals("Обновлённый район", updatedProp.getRegion());
        assertEquals(6_000_000, updatedProp.getPrice(), 0.01);
        assertEquals(30_000, updatedProp.getRent(), 0.01);
        assertEquals(10_000, updatedProp.getTaxes(), 0.01);
        assertEquals(50_000, updatedProp.getRepairCost(), 0.01);
        assertEquals(0.333, updatedProp.getRoi(), 0.01); // ROI = ((30_000 - 10_000) / 6_000_000) * 100 = 0.333...
        assertEquals(302, updatedProp.getPaybackPeriod());  // (6_000_000 + 50_000) / (30_000 - 10_000) = 302.5
    }

    @Test
    void testDelete() {
        int idToDelete = testProperty.getId();

        // Удаляем
        dao.delete(idToDelete);

        // Проверяем, что список пуст
        List<RentalProperty> remaining = dao.getAll();
        assertTrue(remaining.isEmpty(), "После удаления список должен быть пуст");
    }
}