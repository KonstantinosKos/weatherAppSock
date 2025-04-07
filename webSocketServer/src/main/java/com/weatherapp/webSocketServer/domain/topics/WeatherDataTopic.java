package com.weatherapp.webSocketServer.domain.topics;

import com.weatherapp.webSocketServer.domain.wsdata.Coordinates;
import com.weatherapp.webSocketServer.domain.wsdata.Temperature;
import com.weatherapp.webSocketServer.domain.wsdata.WindSpeed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherDataTopic {
    private String city;
    private Coordinates coordinates;
    private String timestamp;
    private Temperature temperature;
    private int humidity;
    private WindSpeed windSpeed;
    private int pressure;
    private String condition;

}
