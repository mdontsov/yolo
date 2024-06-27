package com.websocket.yolo.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websocket.yolo.game.CountDown;
import com.websocket.yolo.game.GameService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

  private final GameService service;

  private final ObjectMapper mapper = new ObjectMapper();

  private final List<WebSocketSession> sessions = Collections.synchronizedList(new ArrayList<>());

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  @Override
  public void afterConnectionEstablished(@NonNull WebSocketSession session) {
    sessions.add(session);
    startGameRound();
  }

  @Override
  protected void handleTextMessage(
      @NonNull WebSocketSession session,
      TextMessage message
  ) throws Exception {
    String payload = message.getPayload();
    Map betData = mapper.readValue(payload, Map.class);
    String player = (String) betData.get("player");
    int number = (int) betData.get("number");
    int amount = (int) betData.get("amount");
    service.placeBet(player, number, amount);
    service.generateWinningNumber();
  }

  private void startGameRound() {
    scheduler.schedule(this::endGameRound, CountDown.DURATION.getLength(), TimeUnit.SECONDS);
  }

  private void endGameRound() {
    if (service
        .getBets()
        .isEmpty()) {
      publishMessage("No bets were placed");
      return;
    }
    Map<String, Double> playerBet = service.processBet();
    List<Map<String, Double>> allBets = new ArrayList<>();

    allBets.add(playerBet);
    publishResults(allBets);
    terminateGameSession();
  }

  private void publishResults(List<Map<String, Double>> results) {
    Map<String, Object> message = new HashMap<>();
    results.forEach(result -> message.put("game", composeMessage(result)));
    String payload;

    try {
      payload = mapper.writeValueAsString(message);
    } catch (IOException e) {
      throw new RuntimeException("Failed to serialize message", e);
    }

    publishMessage(payload);
  }

  private void publishMessage(String message) {
    synchronized (sessions) {
      sessions.forEach(session -> {
        try {
          session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
          log.error(e.getLocalizedMessage());
        }
      });
    }
  }

  private void terminateGameSession() {
    synchronized (sessions) {
      sessions.forEach(session -> {
        try {
          session.close();
        } catch (IOException e) {
          log.error(e.getLocalizedMessage());
        }
      });
      sessions.clear();
    }
  }

  private String composeMessage(Map<String, Double> result) {
    AtomicReference<String> message = new AtomicReference<>("");
    result.forEach((k, v) -> {
      message.set(String.format("Player %s, won %s", k, v));
    });
    return message.toString();
  }
}
