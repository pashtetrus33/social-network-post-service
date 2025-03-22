package ru.skillbox.social_network_post.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import ru.skillbox.social_network_post.dto.ErrorResponse;
import ru.skillbox.social_network_post.exception.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void testHandleEntityNotFoundException() {
        EntityNotFoundException exception = new EntityNotFoundException("Entity not found");

        ErrorResponse response = globalExceptionHandler.notFound(exception);

        assertEquals(HttpStatus.NOT_FOUND.name(), response.getStatus());
        assertEquals("Entity not found", response.getErrorMessage());
    }

    @Test
    void testHandleNegativeLikeCountException() {
        NegativeLikeCountException exception = new NegativeLikeCountException("Negative like count");

        ErrorResponse response = globalExceptionHandler.handleNegativeLikeCount(exception);

        assertEquals(HttpStatus.BAD_REQUEST.name(), response.getStatus());
        assertEquals("Negative like count", response.getErrorMessage());
    }

    @Test
    void testHandleIllegalStateException() {
        IllegalStateException exception = new IllegalStateException("Illegal state");

        ErrorResponse response = globalExceptionHandler.handleIllegalState(exception);

        assertEquals(HttpStatus.BAD_REQUEST.name(), response.getStatus());
        assertEquals("Illegal state", response.getErrorMessage());
    }

    @Test
    void testHandleIdMismatchException() {
        IdMismatchException exception = new IdMismatchException("ID mismatch");

        ErrorResponse response = globalExceptionHandler.handleIdMismatch(exception);

        assertEquals(HttpStatus.BAD_REQUEST.name(), response.getStatus());
        assertEquals("ID mismatch", response.getErrorMessage());
    }

    @Test
    void testHandleMethodArgumentTypeMismatchException() {
        IdMismatchException exception = new IdMismatchException("Failed to convert value for parameter param");

        ErrorResponse response = globalExceptionHandler.handleIdMismatch(exception);

        assertEquals(HttpStatus.BAD_REQUEST.name(), response.getStatus());
        assertEquals("Failed to convert value for parameter param",response.getErrorMessage());
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException exception = mock(org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(new ObjectError("field", "Validation error")));

        ErrorResponse response = globalExceptionHandler.notValid(exception);

        assertEquals(HttpStatus.BAD_REQUEST.name(), response.getStatus());
        assertEquals("Validation error", response.getErrorMessage());
    }

    @Test
    void testHandleMissingServletRequestParameterException() {
        MissingServletRequestParameterException exception = new MissingServletRequestParameterException("param", "String");

        ErrorResponse response = globalExceptionHandler.handleMissingParams(exception);

        assertEquals(HttpStatus.BAD_REQUEST.name(), response.getStatus());
        assertEquals("Required request parameter 'param' for method parameter type String is not present", response.getErrorMessage());
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Illegal argument");

        var response = globalExceptionHandler.handleIllegalArgumentException(exception);

        assertEquals("Illegal argument", response.get("error"));
    }

    @Test
    void testHandleCustomFreignException() {
        CustomFreignException exception = new CustomFreignException("Feign error");

        ErrorResponse response = globalExceptionHandler.handleFeignException(exception);

        assertEquals(HttpStatus.I_AM_A_TEAPOT.name(), response.getStatus());
        assertEquals("Feign error occurred: Feign error", response.getErrorMessage());
    }
}