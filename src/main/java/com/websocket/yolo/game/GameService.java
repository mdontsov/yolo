package com.websocket.yolo.game;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

  private final RandomNumberGenerator numberGenerator;

  private final Map<String, Integer> numbers = new ConcurrentHashMap<>();

  @Getter
  private final Map<String, Double> bets = new ConcurrentHashMap<>();

  private Integer numberThatWon;

  private Player player;

  public synchronized void generateWinningNumber() {
    numberThatWon = numberGenerator.generate();
    log.info(String.format("The number that should be guessed is: %s", numberThatWon));
  }

  public synchronized void placeBet(
      String name,
      int number,
      double amount
  ) {
    player = Player
        .builder()
        .playerName(name)
        .numberToGuess(number)
        .betAmount(amount)
        .build();

    if (!player.hasProperData()) {
      return;
    }
    numbers.put(player.getPlayerName(), player.getNumberToGuess());
    bets.put(player.getPlayerName(), player.getBetAmount());
    log.info(String.format("Player %s tries to guess the number with %s", player.getPlayerName(), player.getNumberToGuess()));
    log.info(String.format("Player %s places a bet of %s to win", player.getPlayerName(), player.getBetAmount()));
  }

  public synchronized Map<String, Double> processBet() {
    Map<String, Double> betResult = new HashMap<>();

    for (Map.Entry<String, Integer> entry : numbers.entrySet()) {
      String playerName = entry.getKey();
      int numberToGuess = entry.getValue();
      betResult.putAll(getGameResult(playerName, numberToGuess));
    }
    numbers.clear();
    bets.clear();
    return betResult;
  }

  private Map<String, Double> getGameResult(
      String playerName,
      int numberToGuess
  ) {
    Map<String, Double> gameResult = new HashMap<>();
    if (numberToGuess == numberThatWon) {
      DecimalFormat df = new DecimalFormat("0.00");
      double wonAmount = Double.parseDouble(df.format(bets.get(playerName) * 9.9));
      gameResult.put(playerName, wonAmount);
      player.setPersonalResult(String.format("Player %s won %s", playerName, wonAmount));
      log.info(player.getPersonalResult());
    } else {
      gameResult.put(playerName, 0.0);
      player.setPersonalResult(String.format("Player %s won 0.0", playerName));
      log.info(player.getPersonalResult());
    }
    return gameResult;
  }
}
