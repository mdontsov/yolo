package com.websocket.yolo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.websocket.yolo.game.GameService;
import com.websocket.yolo.game.Player;
import com.websocket.yolo.game.RandomNumberGenerator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

  @Mock
  private RandomNumberGenerator numberGenerator;

  @InjectMocks
  private GameService service;

  private Player player;

  @BeforeEach
  void setUp() {
    player = Player
        .builder()
        .playerName("Maksim")
        .numberToGuess(5)
        .betAmount(9D)
        .build();
  }

  @Test
  void shouldContainPlayerData() {
    assertThat(player.hasProperData()).isTrue();
  }

  @Test
  void shouldCreateBet() {
    service.placeBet(player.getPlayerName(), player.getNumberToGuess(), player.getBetAmount());
    when(numberGenerator.generate()).thenReturn(1);
    service.generateWinningNumber();

    service
        .getBets()
        .forEach((k, v) -> {
          assertThat(k).isEqualTo(player.getPlayerName());
          assertThat(v).isEqualTo(player.getBetAmount());
        });
  }

  @ParameterizedTest
  @ValueSource(strings = "Player Maksim won 98.1")
  void shouldCreateBetThatWon(String personalResult) {
    service.placeBet(player.getPlayerName(), player.getNumberToGuess(), player.getBetAmount());
    DecimalFormat df = new DecimalFormat("0.00");
    double wonAmount = Double.parseDouble(df.format(player.getBetAmount() * 9.9));

    when(numberGenerator.generate()).thenReturn(5);
    service.generateWinningNumber();

    Map<String, Double> gameResult = service.processBet();

    gameResult.forEach((k, v) -> {
      assertThat(k).isEqualTo(player.getPlayerName());
      assertThat(v).isEqualTo(Double.parseDouble(df.format(wonAmount)));
      player.setPersonalResult(personalResult);
    });

    assertThat(player.getPersonalResult()).isEqualTo(personalResult);
  }

  @ParameterizedTest
  @ValueSource(strings = "Player Maksim won 0.0")
  void shouldCreateBetThatLost(String personalResult) {
    service.placeBet(player.getPlayerName(), player.getNumberToGuess(), player.getBetAmount());

    when(numberGenerator.generate()).thenReturn(9);
    service.generateWinningNumber();

    Map<String, Double> gameResult = service.processBet();

    gameResult.forEach((k, v) -> {
      assertThat(k).isEqualTo(player.getPlayerName());
      assertThat(v).isEqualTo(0.0);
      player.setPersonalResult(personalResult);
    });

    assertThat(player.getPersonalResult()).isEqualTo(personalResult);
  }

  @Test
  void shouldCreateMultipleBetsAndGetMultipleResults() {
    Player next = Player
        .builder()
        .playerName("Igor")
        .numberToGuess(8)
        .betAmount(4D)
        .build();

    service.placeBet(player.getPlayerName(), player.getNumberToGuess(), player.getBetAmount());
    service.placeBet(next.getPlayerName(), next.getNumberToGuess(), next.getBetAmount());

    when(numberGenerator.generate()).thenReturn(8);
    service.generateWinningNumber();

    assertThat(service
        .getBets()
        .size()).isEqualTo(2);

    Map<String, Double> gameResult = service.processBet();
    assertThat(gameResult.size()).isGreaterThan(1);
    List<String> names = new ArrayList<>();
    List<Double> betResults = new ArrayList<>();
    gameResult.forEach((k, v) -> {
      names.add(k);
      betResults.add(v);
    });
    assertThat(names).contains("Maksim", "Igor");
    assertThat(betResults).contains(0.0, 39.6);
  }

  @Test
  void shouldReturnEmptyResult_ifBetNotPlaced() {
    when(numberGenerator.generate()).thenReturn(7);
    service.generateWinningNumber();

    Map<String, Double> gameResult = service.processBet();
    assertThat(gameResult).isEmpty();
  }
}
