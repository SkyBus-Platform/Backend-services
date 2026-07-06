package com.skybus.booking.exception;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //Domain exceptions

    @ExceptionHandler(BookingNotFoundException.class)
    public ProblemDetail handleNotFound(BookingNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BookingCancellationException.class)
    public ProblemDetail handleCancellation(BookingCancellationException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(SeatReservationException.class)
    public ProblemDetail handleSeatConflict(SeatReservationException ex) {
        return problem(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(FlightServiceUnavailableException.class)
    public ProblemDetail handleServiceDown(FlightServiceUnavailableException ex) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    // Feign exceptions — map upstream HTTP errors to our status codes

    @ExceptionHandler(FeignException.Conflict.class)
    public ProblemDetail handleFeignConflict(FeignException.Conflict ex) {
        return problem(HttpStatus.CONFLICT, "Seat is no longer available — please select another seat");
    }

    @ExceptionHandler(FeignException.NotFound.class)
    public ProblemDetail handleFeignNotFound(FeignException.NotFound ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Flight or seat not found — it may have been removed");
    }

    @ExceptionHandler(FeignException.class)
    public ProblemDetail handleFeignGeneric(FeignException ex) {
        log.error("Feign call failed with status {}: {}", ex.status(), ex.getMessage());
        return problem(HttpStatus.BAD_GATEWAY,
                "Could not reach flight service. Please try again shortly.");
    }

    // Validation

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid",
                        (a, b) -> a
                ));
        ProblemDetail pd = problem(HttpStatus.UNPROCESSABLE_ENTITY, "Validation failed");
        pd.setProperty("fieldErrors", errors);
        return pd;
    }

    //Fallback

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    //Helper

    private ProblemDetail problem(HttpStatus status, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}