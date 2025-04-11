package com.weatherapp.webSocketServer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class WeatherKafkaConsumer {
    // Listening to the 'weather.current' topic
    @KafkaListener(topics = "weather.current", groupId = "weather-consumer-group")
    public void consumeWeatherCurrent(String message) {
        log.info("Consumed from weather.current: {}", message);
    }

    // Listening to the 'weather.predictions' topic
    @KafkaListener(topics = "weather.predictions", groupId = "weather-consumer-group")
    public void consumeWeatherPredictions(String message) {
        log.info("Consumed from weather.predictions: {}", message);
    }
}
