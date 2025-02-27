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

    // Включено ли логирование (через конфигурацию)
    @Value("${custom.logging.enabled:true}")
    private boolean loggingEnabled;

    // Уровень логирования (DEBUG, INFO, WARN, ERROR)
    @Value("${custom.logging.level:INFO}")
    private String logLevel;

    @PostConstruct
    public void init() {
        log.info("LoggingAspect initialized! Logging enabled: {}", loggingEnabled);
    }

    // Перехватываем все методы контроллеров и сервисов
    @Around("execution(* ru.skillbox.social_network_post.controller..*(..)) || execution(* ru.skillbox.social_network_post.service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!loggingEnabled) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();
        Object[] args = joinPoint.getArgs();

        // Логирование до выполнения метода
        logAtLevel("Calling method: " + methodName + " with arguments: " + Arrays.toString(args));

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;

        // Логирование после выполнения метода
        logAtLevel("Method " + methodName + " executed in " + executionTime + "ms, return: " + result);

        return result;
    }

    // Метод для логирования на нужном уровне
    private void logAtLevel(String message) {
        switch (logLevel.toUpperCase()) {
            case "DEBUG":
                log.debug(message);
                break;
            case "WARN":
                log.warn(message);
                break;
            case "ERROR":
                log.error(message);
                break;
            default:
                log.info(message);
        }
    }
}