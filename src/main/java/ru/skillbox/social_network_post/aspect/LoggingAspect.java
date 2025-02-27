package ru.skillbox.social_network_post.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


import java.util.Objects;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private final Environment environment;
    private final boolean loggingEnabled;

    public LoggingAspect(Environment environment, @Value("${custom.logging.enabled:false}") boolean loggingEnabled) {
        this.environment = environment;
        this.loggingEnabled = loggingEnabled;
    }

    @Around("execution(* ru.skillbox.social_network_post.controller..*(..)) || execution(* ru.skillbox.social_network_post.service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!loggingEnabled) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();
        Object[] args = joinPoint.getArgs();

        logAtLevel("Calling method: " + methodName + " with arguments: " + Objects.toString(args));

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;

        logAtLevel("Method " + methodName + " executed in " + executionTime + "ms, return: " + Objects.toString(result));
        return result;
    }

    private void logAtLevel(String message) {
        String logLevel = environment.getProperty("custom.logging.level", "INFO").toUpperCase();
        switch (logLevel) {
            case "DEBUG": log.debug(message); break;
            case "WARN": log.warn(message); break;
            case "ERROR": log.error(message); break;
            default: log.info(message);
        }
    }
}