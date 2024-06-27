package com.websocket.yolo.game;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class Player {

  private String playerName;

  private Integer numberToGuess;

  private Double betAmount;

  @Setter
  private String personalResult;

  public boolean hasProperData() {
    return playerName != null && numberToGuess != null && betAmount != null;
  }
}
