package ru.skillbox.social_network_post.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class ExecutionTimeAspect {

    @Value("${custom.logging.enabled:true}")
    private boolean globalLoggingEnabled;

    @Value("${custom.logging.level:INFO}")
    private String globalLogLevel;

    @Around("@annotation(LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogExecutionTime annotation = method.getAnnotation(LogExecutionTime.class);

        // Если enabled = true в аннотации, смотрим глобальную настройку
        boolean enabled = annotation.enabled() && globalLoggingEnabled;

        // Если в аннотации явно задан level, берем его, иначе берем из application.yml
        String level = annotation.level().equals("INFO") ? globalLogLevel : annotation.level();

        if (!enabled) {
            return joinPoint.proceed();
        }

        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;

        logAtLevel(level, "Method " + methodName + " executed in " + executionTime + "ms");
        return result;
    }

    private void logAtLevel(String level, String message) {
        logLevel(level, message, log);
    }

    static void logLevel(String level, String message, Logger log) {
        switch (level.toUpperCase()) {
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