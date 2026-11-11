package dev.fitqueue.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestControllerAdvice 
public class GlobalExceptionHandler {
    

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationError(
        MethodArgumentNotValidException exception){

            Map<String, String> errors = new HashMap<>();

            for(var error : exception.getBindingResult().getFieldErrors()){
                errors.put(error.getField(), error.getDefaultMessage());
            }

            return errors;
        }
}
