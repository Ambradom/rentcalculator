package org.example.rentcalculator.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.rentcalculator.model.RentalProperty;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CianApiDAO {
    public RentalProperty fetchFromCian(String url) {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response.toString());

            // Пример парсинга JSON от ЦИАН
            String region = jsonNode.get("location").get("district").asText();
            double price = jsonNode.get("price").get("value").asDouble();
            double rent = jsonNode.get("rent").get("avg").asDouble();

            return new RentalProperty(region, price, rent, 10000, 100000);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}