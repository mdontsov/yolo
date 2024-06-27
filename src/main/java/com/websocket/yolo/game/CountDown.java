package com.websocket.yolo.game;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CountDown {

  DURATION(10);

  private final int length;
}
