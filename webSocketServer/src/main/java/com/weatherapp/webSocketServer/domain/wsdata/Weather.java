package com.weatherapp.webSocketServer.domain.wsdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Weather {

    @JsonProperty("city")
    private String city;
    @JsonProperty("coordinates")
    private Coordinates coordinates;
    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("temperature")
    private Temperature temperature;
    @JsonProperty("humidity")
    private double humidity;
    @JsonProperty("wind_speed")
    private WindSpeed windSpeed;
    @JsonProperty("pressure")
    private int pressure;
    @JsonProperty("condition")
    private String condition;
    @JsonProperty("predicted")
    private Boolean predicted;
}
