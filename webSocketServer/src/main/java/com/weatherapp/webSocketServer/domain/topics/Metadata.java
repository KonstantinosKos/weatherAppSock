package com.weatherapp.webSocketServer.domain.topics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Metadata {
    private String timestamp;
    private String generatedBy;
}