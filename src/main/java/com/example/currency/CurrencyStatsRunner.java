package com.example.currency;


import com.example.currency.CurrencyRate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class CurrencyStatsRunner implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void run(String... args) throws Exception {
        Map<String, List<CurrencyRate>> history = new HashMap<>();

        for (int i = 0; i < 90; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            String url = "https://www.cbr.ru/scripts/XML_daily.asp?date_req=" + formatter.format(date);
            try {
                DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                String xml = restTemplate.getForObject(url, String.class);
                if (xml == null) continue;

                Document doc = dBuilder.parse(new java.io.ByteArrayInputStream(xml.getBytes()));
                doc.getDocumentElement().normalize();
                NodeList nodes = doc.getElementsByTagName("Valute");

                for (int j = 0; j < nodes.getLength(); j++) {
                    Element element = (Element) nodes.item(j);
                    String code = element.getElementsByTagName("CharCode").item(0).getTextContent();

                    int nominal = Integer.parseInt(
                            element.getElementsByTagName("Nominal").item(0).getTextContent());

                    String valueStr = element.getElementsByTagName("Value").item(0)
                            .getTextContent().replace(",", ".");

                    double rawValue = Double.parseDouble(valueStr);
                    double normalizedValue = rawValue / nominal;

                    history.computeIfAbsent(code, k -> new ArrayList<>())
                            .add(new CurrencyRate(code, normalizedValue, date));
                }
            } catch (Exception e) {
                System.err.println("Error processing " + date + ": " + e.getMessage());
            }
        }

        CurrencyRate max = null, min = null;
        double total = 0;
        int count = 0;

        for (List<CurrencyRate> list : history.values()) {
            for (CurrencyRate rate : list) {
                if (max == null || rate.getValue() > max.getValue()) max = rate;
                if (min == null || rate.getValue() < min.getValue()) min = rate;
                total += rate.getValue();
                count++;
            }
        }

        System.out.println("\n===== Currency Statistics (Last 90 Days) =====\n");

        System.out.printf("Highest rate:\n%-8s | %.4f | %s\n\n",
                max.getCode(), max.getValue(), max.getDate());

        System.out.printf("Lowest rate:\n%-8s | %.4f | %s\n\n",
                min.getCode(), min.getValue(), min.getDate());

        System.out.printf("Average ruble value across all currencies: %.4f\n", total / count);
    }
}

