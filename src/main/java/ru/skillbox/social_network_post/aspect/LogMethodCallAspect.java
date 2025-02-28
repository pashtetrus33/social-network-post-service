package ru.skillbox.social_network_post.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LogMethodCallAspect {

    @Value("${custom.logging.enabled:true}")
    private boolean globalLoggingEnabled;

    @Value("${custom.logging.level:INFO}")
    private String globalLogLevel;

    @Around("@annotation(LogMethodCall)")
    public Object logMethodCall(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogMethodCall annotation = method.getAnnotation(LogMethodCall.class);

        // Если enabled = true в аннотации, смотрим глобальную настройку
        boolean enabled = annotation.enabled() && globalLoggingEnabled;

        // Если в аннотации явно задан level, берем его, иначе берем из application.yml
        String level = annotation.level().equals("INFO") ? globalLogLevel : annotation.level();

        if (!enabled) {
            return joinPoint.proceed();
        }

        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();
        Object[] args = joinPoint.getArgs();
        logAtLevel(level, "Calling method: " + methodName + " with arguments: " + Arrays.toString(args));

        return joinPoint.proceed();
    }

    private void logAtLevel(String level, String message) {
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