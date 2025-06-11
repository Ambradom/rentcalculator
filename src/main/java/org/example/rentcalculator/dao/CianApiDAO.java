package org.example.rentcalculator.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.rentcalculator.model.RentalProperty;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CianApiDAO {

    // Путь к тестовому файлу
    private static final String TEST_JSON_PATH = "src/test-cian.json";

    /**
     * Загружает данные с ЦИАН или использует тестовый JSON как резерв
     */
    public RentalProperty fetchFromCian(String url) throws IOException {
        try {
            if (url == null || url.isEmpty()) {
                System.out.println("URL пуст — используем тестовые данные");
                return null; // ← явно возвращаем null
            } else if (url.startsWith("http")) {
                return loadFromUrl(url);
            } else {
                return loadFromFile(url);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке данных: " + e.getMessage());
            return null;
        }
    }

    /**
     * Парсинг JSON по URL
     */
    private RentalProperty loadFromUrl(String urlStr) throws Exception {
        URL obj = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.toString());

        return parseJson(jsonNode);
    }

    /**
     * Чтение JSON из файла
     */
    private RentalProperty loadFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            System.err.println("Файл не найден: " + path.toAbsolutePath());
            return null;
        }

        if (Files.isDirectory(path)) {
            System.err.println();
            return null;
        }

        byte[] jsonData = Files.readAllBytes(path);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonData);

        return parseJson(jsonNode);
    }
    /**
     * Используется для тестовых данных
     */
    private RentalProperty loadFromTestFile() throws IOException {
        return loadFromFile(TEST_JSON_PATH);
    }

    /**
     * Безопасный парсинг JSON
     */
    private RentalProperty parseJson(JsonNode jsonNode) {
        try {
            String region = jsonNode.get("location").get("district").asText();
            double price = jsonNode.get("price").get("value").asDouble();
            double rent = jsonNode.get("rent").get("avg").asDouble();

            return new RentalProperty(region, price, rent, 10_000, 50_000);
        } catch (Exception e) {
            System.err.println("Неверный формат JSON: " + e.getMessage());
            return null;
        }
    }
}