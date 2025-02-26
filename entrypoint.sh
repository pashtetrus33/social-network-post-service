#!/bin/sh

# Создаем папку для логов, если она не существует
mkdir -p "$LOG_PATH"

# Запускаем приложение с правильным указанием конфигурации Logback
exec java -Dlogging.config=/apps/logback-spring.xml -jar /apps/app.jar