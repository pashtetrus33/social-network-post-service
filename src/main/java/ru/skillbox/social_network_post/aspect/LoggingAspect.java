package ru.skillbox.social_network_post.aspect;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Value("${custom.logging.enabled:true}")
    private boolean loggingEnabled;

    @Value("${custom.logging.level:INFO}")
    private String logLevel;

    @PostConstruct
    public void init() {
        log.info("LoggingAspect initialized! Logging enabled: {}", loggingEnabled);
    }

    @Around("execution(* ru.skillbox.social_network_post.controller..*(..)) || execution(* ru.skillbox.social_network_post.service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!loggingEnabled) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();
        Object[] args = joinPoint.getArgs();

        logAtLevel("Calling method: " + methodName + " with arguments: " + Arrays.toString(args));

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;

        logAtLevel("Method " + methodName + " executed in " + executionTime + "ms, return: " + result);

        return result;
    }

    private void logAtLevel(String message) {
        String colorReset = "\u001B[0m"; // Сброс цвета
        String color;

        switch (logLevel.toUpperCase()) {
            case "DEBUG":
                color = "\u001B[36m"; // Голубой
                log.debug(color + message + colorReset);
                break;
            case "WARN":
                color = "\u001B[33m"; // Желтый
                log.warn(color + message + colorReset);
                break;
            case "ERROR":
                color = "\u001B[31m"; // Красный
                log.error(color + message + colorReset);
                break;
            default:
                color = "\u001B[32m"; // Зеленый (INFO)
                log.info(color + message + colorReset);
        }
    }
}