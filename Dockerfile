FROM openjdk:17-jdk-alpine3.14

ARG JAR_FILE

# Создаем директорию для приложения и логов
RUN mkdir -p /apps /var/logs

# Копируем JAR файл, конфигурацию логов (logback-spring.xml), настройки bootstrap.yml и entrypoint.sh
COPY ./target/${JAR_FILE} /apps/app.jar
COPY ./target/classes/logback-spring.xml /apps/logback-spring.xml
COPY ./target/classes/bootstrap.yml /apps/bootstrap.yml
COPY ./entrypoint.sh /apps/entrypoint.sh

# Делаем entrypoint.sh исполнимым
RUN chmod +x /apps/entrypoint.sh

# Указываем переменные окружения по умолчанию
ENV CUSTOM_LOGGING_ENABLED=true
ENV CUSTOM_LOGGING_LEVEL=INFO

# Запускаем контейнер с entrypoint.sh
CMD ["/apps/entrypoint.sh"]