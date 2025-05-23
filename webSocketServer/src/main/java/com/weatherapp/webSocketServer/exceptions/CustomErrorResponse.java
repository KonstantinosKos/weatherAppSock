package com.weatherapp.webSocketServer.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class CustomErrorResponse extends RuntimeException{
    private HttpStatus status;
    private String message;
}