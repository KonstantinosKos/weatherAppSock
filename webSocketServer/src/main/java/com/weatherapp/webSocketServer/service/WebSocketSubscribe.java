package com.weatherapp.webSocketServer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.weatherapp.webSocketServer.domain.topics.WeatherDataTopic;
import com.weatherapp.webSocketServer.exceptions.CustomErrorResponse;
import com.weatherapp.webSocketServer.payload.ws.WeatherPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.net.URI;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.weatherapp.webSocketServer.utils.Utils.convert;
import static com.weatherapp.webSocketServer.utils.Utils.convertPayloadToWeather;

@Component
@Slf4j
public class WebSocketSubscribe implements WebSocketHandler {

    private static final String WEBSOCKET_URL = "ws://localhost:8000/ws";
    private static final int RECONNECT_DELAY = 5; // seconds

    private final AtomicReference<WebSocketSession> session = new AtomicReference<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final WeatherKafkaProducer weatherKafkaProducer;

    public WebSocketSubscribe(WeatherKafkaProducer weatherKafkaProducer) {
        this.weatherKafkaProducer = weatherKafkaProducer;
        connect();
    }

    private void connect() {
        StandardWebSocketClient client = new StandardWebSocketClient();
        CompletableFuture<WebSocketSession> futureSession =
                client.execute(this, String.valueOf(URI.create(WEBSOCKET_URL)));

        futureSession.whenComplete((webSocketSession, throwable) -> {
            if (throwable != null) {
                log.error("WebSocket connection failed: {}", throwable.getMessage());
                scheduleReconnect();
            } else {
                session.set(webSocketSession);
                log.info("Connected to WebSocket server.");
            }
        });
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        session.set(webSocketSession);
        log.info("WebSocket connection established.");
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        try {
            convertPayloadToWeather(
                    String.valueOf(message.getPayload()));
//            weatherKafkaProducer.sendCurrentWeather(convert(
//                    convertPayloadToWeather(
//                            String.valueOf(message.
//                                    getPayload()))));
            weatherKafkaProducer.sendCurrentWeather(convertPayloadToWeather( String.valueOf(message.
                                    getPayload())));
            log.info("Received message at: {}",
                    convertPayloadToWeather(
                            String.valueOf(
                                    message.
                                            getPayload())).getTimestamp());
        } catch (JsonProcessingException e) {
            throw new CustomErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket error: {}", exception.getMessage());
        closeAndReconnect();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.warn("WebSocket connection closed: {}. Reconnecting...", status);
        closeAndReconnect();
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void closeAndReconnect() {
        WebSocketSession currentSession = session.getAndSet(null);
        if (currentSession != null && currentSession.isOpen()) {
            try {
                currentSession.close();
            } catch (Exception e) {
                log.error("Error closing WebSocket session: {}", e.getMessage());
            }
        }
        scheduleReconnect();
    }

    private void scheduleReconnect() {
        scheduler.schedule(this::connect, RECONNECT_DELAY, TimeUnit.SECONDS);
        log.info("Reconnecting in {} seconds...", RECONNECT_DELAY);
    }
}
