package com.weatherapp.webSocketServer.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherapp.webSocketServer.domain.topics.WeatherDataTopic;
import com.weatherapp.webSocketServer.payload.ws.WeatherPayload;

import java.util.List;
import java.util.stream.Collectors;


public class Utils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static WeatherPayload convertPayloadToWeather(String payload) throws JsonProcessingException {
        return objectMapper.readValue(payload, WeatherPayload.class);
    }

    public static List<WeatherDataTopic> convert(WeatherPayload weatherPayloads) {
        return weatherPayloads.getCurrentWeather().
                stream().
                map(payload -> objectMapper.convertValue(payload, WeatherDataTopic.class)).
                collect(Collectors.toList());
    }
}
