package com.weatherapp.webSocketServer.domain.wsdata;

import lombok.Data;

@Data
public class Temperature {

    private double celsius;
    private double fahrenheit;
}
