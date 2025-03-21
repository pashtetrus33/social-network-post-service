package ru.skillbox.social_network_post.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
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

    @Before("@annotation(LogMethodCall)")
    public void logMethodCall(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogMethodCall annotation = method.getAnnotation(LogMethodCall.class);

        boolean enabled = annotation.enabled() && globalLoggingEnabled;
        if (!enabled) {
            return;
        }

        String level = annotation.level().isEmpty() ? globalLogLevel : annotation.level();

        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();
        Object[] args = joinPoint.getArgs();
        String message = "Calling method: " + methodName + " with arguments: " + Arrays.toString(args);

        logAtLevel(level, message);
    }

    private void logAtLevel(String level, String message) {
        ExecutionTimeAspect.logLevel(level, message, log);
    }
}