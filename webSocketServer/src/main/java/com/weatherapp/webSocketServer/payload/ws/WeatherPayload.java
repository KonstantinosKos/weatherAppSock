package com.weatherapp.webSocketServer.payload.ws;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.weatherapp.webSocketServer.domain.wsdata.Weather;
import lombok.Data;

import java.util.List;

@Data
public class WeatherPayload {
    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("generated_by")
    private String generatedBy;
    @JsonProperty("current_weather")
    private List<Weather> currentWeather;
    @JsonProperty("predictions")
    private List<Weather> predictions;
}
