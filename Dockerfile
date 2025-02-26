FROM openjdk:17-jdk-alpine3.14

ARG JAR_FILE

# Создаем директорию для приложения и логов
RUN mkdir -p /apps /var/logs

# Копируем JAR файл и entrypoint.sh
COPY ./target/${JAR_FILE} /apps/app.jar
COPY ./entrypoint.sh /apps/entrypoint.sh

# Делаем entrypoint.sh исполнимым
RUN chmod +x /apps/entrypoint.sh

# Указываем переменную окружения для пути логов
ENV LOG_PATH=/var/logs
ENV LOGGING_ENABLED=true
ENV LOG_LEVEL=INFO

# Открываем порт, если необходимо для приложения
EXPOSE 8761

# Запускаем контейнер с entrypoint.sh
CMD ["/apps/entrypoint.sh"]