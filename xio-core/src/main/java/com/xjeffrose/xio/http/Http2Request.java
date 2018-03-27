package com.xjeffrose.xio.http;

import brave.Span;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2Headers;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Http2Request<T> implements Traceable {

  final int streamId;
  final T payload;
  final boolean eos;
  private final Span span;

  public Http2Request(int streamId, T payload, boolean eos, @Nullable Span span) {
    this.streamId = streamId;
    this.payload = payload;
    this.eos = eos;
    this.span = span;
  }

  public static Http2Request<Http2DataFrame> build(int streamId, Http2DataFrame data, boolean eos) {
    return new Http2Request<>(streamId, data, eos, null);
  }

  public static Http2Request<Http2Headers> build(
      int streamId, Http2Headers headers, boolean eos, Span span) {
    return new Http2Request<>(streamId, headers, eos, span);
  }

  @Nullable
  @Override
  public Span traceSpan() {
    return span;
  }
}
