package com.xjeffrose.xio.tracing;

import brave.Tracing;
import brave.context.slf4j.MDCCurrentTraceContext;
import brave.sampler.Sampler;
import com.typesafe.config.Config;
import zipkin.Span;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

public class FakeTracer extends XioTracing {

  BlockingQueue<Span> spans = new LinkedBlockingQueue<>();

  public FakeTracer(Config config) {
    super(config);
  }

  @Override
  Tracing buildTracing(String name, String zipkinUrl, float samplingRate) {
    return Tracing.newBuilder()
      .reporter(span -> spans.offer(span))
      .currentTraceContext(MDCCurrentTraceContext.create())
      .sampler(Sampler.ALWAYS_SAMPLE)
      .build();
  }

  public List<Span> spansResult(int expectedCount) {
    List<Span> spansResult = new ArrayList<>();
    for (int i = 0; i < expectedCount; i++) {
      try {
        spansResult.add(spans.poll(5, TimeUnit.SECONDS));
      } catch (InterruptedException e) {
        fail("timeout");
        break;
      }
    }
    return spansResult;
  }

  public void verifyCount(int count) {
    List<Span> result = spansResult(count);
    assertEquals(count, result.size());
  }
}
