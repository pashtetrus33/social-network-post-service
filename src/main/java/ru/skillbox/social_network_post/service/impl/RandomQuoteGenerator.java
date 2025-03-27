package ru.skillbox.social_network_post.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ru.skillbox.social_network_post.exception.QuoteRetrievalException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public class RandomQuoteGenerator {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    static ObjectMapper objectMapper = new ObjectMapper();

    private static final String API_URL = "https://api.forismatic.com/api/1.0/?method=getQuote&format=jsonp&jsonp=?&lang=ru";

    // Private constructor to prevent instantiation
    private RandomQuoteGenerator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }


    public static String getRandomQuote() {

        try {

            String response = getString(API_URL);
            log.info("API response: {}", response);

            // Убираем обертку JSONP
            response = response.substring(2, response.length() - 1);

            JsonNode jsonNode = objectMapper.readTree(response);
            String quoteText = jsonNode.get("quoteText").asText();
            String quoteAuthor = jsonNode.get("quoteAuthor").asText();
            return "\"" + quoteText + "\" — " + quoteAuthor;
        } catch (IOException e) {
            throw new QuoteRetrievalException("Ошибка при получении цитаты", e);
        }
    }

    public static String getString(String url) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(java.time.Duration.ofSeconds(5)) // Таймаут на запрос
                .header("Accept", "application/json") // Можно добавить заголовки
                .build();

        HttpResponse<String> response;
        try {
            response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            // Обработка IOException
            throw new IOException(e.getMessage(), e);
        } catch (InterruptedException e) {
            // Восстанавливаем статус прерывания
            Thread.currentThread().interrupt();
            // Обработка InterruptedException, оборачиваем в IOException
            throw new IOException("Thread was interrupted", e);
        }

        if (response.statusCode() != 200) { // Проверяем код ответа
            throw new IOException("HTTP error code: " + response.statusCode());
        }

        return response.body();
    }
}