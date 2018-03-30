package com.xjeffrose.xio.http.internal;

import brave.Span;
import com.xjeffrose.xio.http.FullRequest;
import com.xjeffrose.xio.http.Headers;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpUtil;
import javax.annotation.Nullable;

public class FullHttp1Request implements FullRequest {

  private final FullHttpRequest delegate;
  private final Http1Headers headers;
  private final Span span;

  public FullHttp1Request(FullHttpRequest delegate, @Nullable Span span) {
    this.delegate = delegate;
    this.headers = new Http1Headers(delegate.headers());
    this.span = span;
  }

  public FullHttp1Request(FullHttpRequest delegate) {
    this(delegate, null);
  }

  // region Request

  @Override
  public boolean startOfStream() {
    return true;
  }

  @Override
  public HttpMethod method() {
    return delegate.method();
  }

  @Override
  public String path() {
    return delegate.uri();
  }

  @Override
  public String version() {
    return delegate.protocolVersion().text();
  }

  @Override
  public Headers headers() {
    return headers;
  }

  @Override
  public int streamId() {
    return -1;
  }

  @Override
  public boolean keepAlive() {
    return HttpUtil.isKeepAlive(delegate);
  }

  @Override
  public boolean hasBody() {
    return delegate.content() != null && delegate.content().readableBytes() > 0;
  }

  @Override
  public ByteBuf body() {
    return delegate.content();
  }

  // endregion

  // region Traceable

  @Nullable
  @Override
  public Span traceSpan() {
    return null;
  }

  // endregion
}
