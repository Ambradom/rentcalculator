package org.example.rentcalculator.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.rentcalculator.model.RentalProperty;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CianApiDAO {

    private static final String TEST_JSON_PATH = "src/test-cian.json";

    public RentalProperty fetchFromCian(String url) throws IOException {
        try {
            if (url == null || url.isEmpty()) {
                System.out.println("URL пуст — используем тестовые данные");
                return loadFromTestFile(); // ← измените на loadFromTestFile()
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
     * Парсит HTML-страницу ЦИАН и возвращает объект недвижимости
     */
    private RentalProperty parseFromHtml(String html) {
        Document doc = Jsoup.parse(html);

        // Пример: Извлечение цены
        String priceText = "";
        Element priceElement = doc.selectFirst(".a10a3f92e9--price--wsOWH"); // CSS-класс из ЦИАН
        if (priceElement != null) {
            priceText = priceElement.text();
        }

        // Пример: Извлечение района
        String district = "Неизвестно";
        Elements addressItems = doc.select(".a10a3f92e9--address-item--UEAG3");
        if (!addressItems.isEmpty()) {
            district = addressItems.get(addressItems.size() - 2).text(); // предпоследний элемент часто содержит район
        }

        // Пример: Извлечение характеристик
        double price = parsePrice(priceText);
        double rent = 0; // на ЦИАН аренда обычно не указана, но можно искать отдельно

        return new RentalProperty(district, price, rent, 10_000, 50_000);
    }

    /**
     * Преобразует строку вида "10 млн ₽" в число
     */
    private double parsePrice(String priceText) {
        if (priceText == null || priceText.isBlank()) return 0;

        try {
            // Убираем лишние символы
            String cleaned = priceText.replaceAll("[^\\d]", "");
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            System.err.println("Ошибка парсинга цены: " + e.getMessage());
            return 0;
        }
    }

    private RentalProperty loadFromUrl(String urlStr) throws Exception {
        URL obj = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Максимально похоже на браузер
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String html = response.toString();

        // Ищем JSON внутри HTML
        int start = html.indexOf("window._cianConfig=");
        if (start == -1) {
            throw new IOException("JSON не найден на странице");
        }

        int end = html.indexOf("</script>", start);
        String jsonPart = html.substring(start + 18, end).trim(); // 'window._cianConfig=' = 18 символов
        if (jsonPart.endsWith(";")) {
            jsonPart = jsonPart.substring(0, jsonPart.length() - 1); // удаляем точку с запятой
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

            return new RentalProperty(region, price, rent, 10_000, 50_000);

        } catch (Exception e) {
            System.err.println("Неверный формат JSON: " + e.getMessage());
            return null;
        }
    }

}