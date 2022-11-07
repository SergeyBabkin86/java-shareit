package ru.practicum.shareit.error;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void handleEmailConflictException() {
        var exception = new EmailValidationException("Email validation error");
        ErrorResponse response = errorHandler.handleEmailConflictException(exception);
        assertNotNull(response);
        assertEquals(response.getMessage(), exception.getMessage());
    }

    @Test
    void handleEntityNotFoundExceptionTest() {
        var exception = new EntityNotFoundException("NOT_FOUND");

        ErrorResponse response = errorHandler.handleEntityNotFoundException(exception);

        assertNotNull(response);
        assertEquals(response.getMessage(), exception.getMessage());
    }

    @Test
    void handleItemTransactionExceptionTest() {
        var exception = new ItemTransactionException("NOT_AVAILABLE");

        ErrorResponse response = errorHandler.handleItemTransactionException(exception);

        assertNotNull(response);
        assertEquals(response.getMessage(), exception.getMessage());
    }

    @Test
    void handleBookingTransactionExceptionTest() {
        var exception = new BookingTransactionException("message");

        ErrorResponse response = errorHandler.handleBookingTransactionException(exception);

        assertNotNull(response);
        assertEquals(response.getMessage(), exception.getMessage());
    }
}