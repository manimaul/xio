package com.xjeffrose.xio.http;

import brave.Span;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2Headers;
import javax.annotation.Nonnull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
public class Http2Response<T> implements Traceable {

  final int streamId;
  final T payload;
  final boolean eos;
  private final Span span;

  public Http2Response(int streamId, T payload, boolean eos, Span span) {
    this.streamId = streamId;
    this.payload = payload;
    this.eos = eos;
    this.span = span;
  }

  public static Http2Response<Http2DataFrame> build(
      int streamId, Http2DataFrame data, boolean eos) {
    return new Http2Response<>(streamId, data, eos, null); // todo: WBK
  }

  public static Http2Response<Http2Headers> build(int streamId, Http2Headers headers) {
    return new Http2Response<>(streamId, headers, false, null); // todo: WBK
  }

  public static Http2Response<Http2Headers> build(int streamId, Http2Headers headers, boolean eos) {
    return new Http2Response<>(streamId, headers, eos, null); // todo: WBK
  }

  public Http2Response newStreamId(int newId) {
    return new Http2Response<>(newId, payload, eos, span);
  }

  @Nonnull
  @Override
  public Span traceSpan() {
    return span;
  }
}
