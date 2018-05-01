package com.xjeffrose.xio.client.asyncretry;

import io.netty.channel.EventLoopGroup;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncRetryLoop {
  private final int attemptLimit;
  private final EventLoopGroup eventLoopGroup;
  private final long delay;
  private final TimeUnit unit;
  private int attemptCount = 0;

  public AsyncRetryLoop(
      int attemptLimit, EventLoopGroup eventLoopGroup, long delay, TimeUnit unit) {
    this.attemptLimit = attemptLimit;
    this.eventLoopGroup = eventLoopGroup;
    this.delay = delay;
    this.unit = unit;
  }

  public void attempt(Runnable action) {
    attemptCount++;
    if (attemptCount == 1) {
      action.run();
    } else {
      eventLoopGroup.schedule(action, delay, unit);
    }
  }

  public boolean canRetry() {
    return attemptCount < attemptLimit;
  }
}
