#!/bin/sh

# Создаем папку для логов, если она не существует
mkdir -p "$LOG_PATH"

# Убедимся, что переменная окружения LOG_PATH передана корректно
echo "Using LOG_PATH: $LOG_PATH"
echo "Using LOG_LEVEL: ${LOG_LEVEL:-INFO}"

# Запускаем приложение с правильным указанием конфигурации Logback
exec java -Dlogging.config=/apps/logback-spring.xml -DLOG_PATH=$LOG_PATH -DLOG_LEVEL=${LOG_LEVEL:-INFO} -jar /apps/app.jar