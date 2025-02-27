#!/bin/sh

# Устанавливаем путь к логам по умолчанию, если он не задан
LOG_PATH=${LOG_PATH:-/var/logs}

# Создаем папку для логов, если её нет
mkdir -p "$LOG_PATH"

# Логируем переменные окружения
echo "Using LOG_PATH: $LOG_PATH"
echo "Using LOG_LEVEL: ${LOG_LEVEL:-INFO}"

# Запускаем приложение с правильными параметрами
exec java \
  -Dlogging.config=/apps/logback-spring.xml \
  -Dcustom.logging.path=$LOG_PATH \
  -Dcustom.logging.level=${LOG_LEVEL:-INFO} \
  -Dspring.config.location=/apps/bootstrap.yml \
  -jar /apps/app.jar