FROM openjdk:17-jdk-alpine3.14

ARG JAR_FILE

# Создаем директории для приложения и логов
RUN mkdir -p /apps /var/logs

# Копируем JAR файл, конфигурацию логов, настройки и скрипт запуска
COPY ./target/${JAR_FILE} /apps/app.jar
COPY ./target/classes/logback-spring.xml /apps/logback-spring.xml
COPY ./target/classes/bootstrap.yml /apps/bootstrap.yml
COPY ./entrypoint.sh /apps/entrypoint.sh

# Делаем скрипт запуска исполнимым
RUN chmod +x /apps/entrypoint.sh

# Устанавливаем переменные окружения по умолчанию
ENV LOG_PATH=/var/logs
ENV LOG_LEVEL=INFO

# Запускаем контейнер через entrypoint.sh
CMD ["/apps/entrypoint.sh"]