package com.websocket.yolo.game;

import java.util.Random;
import org.springframework.stereotype.Component;

@Component
public class RandomNumberGenerator {

  private final Random random = new Random();

  public int generate() {
    return random.nextInt(10) + 1;
  }
}
