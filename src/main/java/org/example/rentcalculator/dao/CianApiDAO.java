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

    private static final String TEST_JSON_PATH = "src/test-cian.json";

    public RentalProperty fetchFromCian(String url) throws IOException {
        if (url == null || url.isBlank()) {
            System.out.println("URL пуст — используем тестовые данные");
            return loadFromTestFile();
        }

        try {
            if (url.startsWith("http")) {
                return loadFromUrl(url);
            } else {
                return loadFromFile(url);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке данных: " + e.getMessage());
            return null;
        }
    }

    private RentalProperty loadFromUrl(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        // Извлечение JSON из HTML (упрощённо)
        String html = response.toString();
        int start = html.indexOf("window._cianConfig=");
        if (start == -1) {
            throw new IOException("Не найден JSON в ответе");
        }

        int end = html.indexOf("</script>", start);
        String jsonPart = html.substring(start + 18, end).trim(); // 18 — длина 'window._cianConfig='
        if (jsonPart.endsWith(";")) {
            jsonPart = jsonPart.substring(0, jsonPart.length() - 1);
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonPart);

        return parseJson(root);
    }

    private RentalProperty loadFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Файл не найден: " + path.toAbsolutePath());
        }

        byte[] jsonData = Files.readAllBytes(path);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonData);

        return parseJson(root);
    }

    private RentalProperty loadFromTestFile() throws IOException {
        return loadFromFile(TEST_JSON_PATH);
    }

    private RentalProperty parseJson(JsonNode jsonNode) {
        try {
            String region = "Неизвестно";
            if (jsonNode.has("location")) {
                JsonNode locationNode = jsonNode.get("location");
                if (locationNode.has("district")) {
                    region = locationNode.get("district").asText();
                }
            }

            double price = 0;
            if (jsonNode.has("price")) {
                JsonNode priceNode = jsonNode.get("price");
                if (priceNode.has("value")) {
                    price = priceNode.get("value").asDouble();
                }
            }

            double rent = 0;
            if (jsonNode.has("rent")) {
                JsonNode rentNode = jsonNode.get("rent");
                if (rentNode.has("avg")) {
                    rent = rentNode.get("avg").asDouble();
                }
            }

            // Создаем объект недвижимости
            return new RentalProperty(region, price, rent, 10_000, 50_000);

        } catch (Exception e) {
            System.err.println("Неверный формат JSON: " + e.getMessage());
            return null;
        }
    }
}