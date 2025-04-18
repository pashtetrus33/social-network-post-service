package ru.skillbox.social_network_post.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.skillbox.social_network_post.exception.QuoteRetrievalException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;


@Slf4j
@Service
public class RandomQuoteGenerator {

    static ObjectMapper objectMapper = new ObjectMapper();

    public List<String> getRandomQuote() {
        try {
            String response = getString(new URL("https://api.forismatic.com/api/1.0/?method=getQuote&format=jsonp&jsonp=?&lang=ru"));
            log.info("API response: {}", response);

            // Убираем обертку JSONP
            response = response.substring(2, response.length() - 1);

            JsonNode jsonNode = objectMapper.readTree(response);
            String quoteText = jsonNode.get("quoteText").asText();
            String quoteAuthor = jsonNode.get("quoteAuthor").asText();

            return Arrays.asList(quoteAuthor, quoteText);
        } catch (IOException e) {
            throw new QuoteRetrievalException("Ошибка при получении цитаты", e);
        }
    }


    private String getString(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();

        return content.toString();
    }
}