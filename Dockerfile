FROM openjdk:17-jdk-alpine3.14

ARG JAR_FILE=post-service.jar
ENV JAR_PATH=/apps/post-service.jar

WORKDIR /apps

COPY ./target/${JAR_FILE} ${JAR_PATH}
COPY ./entrypoint.sh /apps/entrypoint.sh
RUN chmod +x /apps/entrypoint.sh

EXPOSE 8083
ENTRYPOINT ["/apps/entrypoint.sh"]