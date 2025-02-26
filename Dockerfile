FROM openjdk:17-jdk-alpine3.14

ARG JAR_FILE

# Создаем директорию для приложения и логов
RUN mkdir -p /apps /var/logs

# Копируем JAR файл, конфигурацию логов и entrypoint.sh
COPY ./target/${JAR_FILE} /apps/app.jar
COPY ./target/classes/logback-spring.xml /apps/logback-spring.xml
COPY ./entrypoint.sh /apps/entrypoint.sh

# Делаем entrypoint.sh исполнимым
RUN chmod +x /apps/entrypoint.sh

# Указываем переменные окружения
ENV LOG_PATH=/var/logs
ENV LOGGING_ENABLED=true
ENV LOG_LEVEL=INFO

# Запускаем контейнер с entrypoint.sh
CMD ["/apps/entrypoint.sh"]