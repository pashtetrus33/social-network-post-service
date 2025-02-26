#!/bin/sh

# Создаем папку для логов, если она не существует
mkdir -p $LOG_PATH

# Запускаем приложение
exec java -Dlogback.configurationFile=/apps/logback-spring.xml -jar /apps/app.jar