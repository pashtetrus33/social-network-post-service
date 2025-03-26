package ru.skillbox.social_network_post.service.impl;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;

@Slf4j
public class RandomQuoteGenerator {

    private static final String API_URL = "https://api.forismatic.com/api/1.0/?method=getQuote&format=jsonp&jsonp=?&lang=ru";

    public static String getRandomQuote() {
        URL url;
        try {
            url = new URL(API_URL);
            String response = getString(url);
            String quoteText = response.substring(response.indexOf("\"quoteText\":\"") + 12, response.indexOf("\",\"quoteAuthor\""));
            String quoteAuthor = response.substring(response.indexOf("\"quoteAuthor\":\"") + 15, response.indexOf("\"}", response.indexOf("\"quoteAuthor\":\"")));
            return "\"" + quoteText + "\" — " + quoteAuthor;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getString(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();

        // Преобразуем полученный JSON-ответ в строку цитаты
        return content.toString();
    }
}