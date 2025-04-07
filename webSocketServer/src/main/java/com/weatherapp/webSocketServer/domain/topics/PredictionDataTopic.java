package com.weatherapp.webSocketServer.domain.topics;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PredictionDataTopic extends WeatherDataTopic{
    private boolean predicted = true;
}