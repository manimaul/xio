package com.xjeffrose.xio.http;

import com.google.auto.value.AutoValue;
import com.xjeffrose.xio.core.internal.UnstableApi;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Builder;
import lombok.ToString;

/** Value class for representing an outgoing HTTP1/2 Request, for use in a client. */
@UnstableApi
@AutoValue
@ToString
public abstract class DefaultFullRequest implements FullRequest {

  public abstract ByteBuf body();

  public abstract HttpMethod method();

  public abstract String path();

  public abstract Headers headers();

  /** Not intended to be called. */
  @Override
  public String version() {
    return "";
  }

  @Override
  public boolean keepAlive() {
    return false;
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder body(ByteBuf body);

    public abstract Builder method(HttpMethod method);

    public abstract Builder path(String path);

    public abstract Builder headers(Headers headers);

    public abstract DefaultFullRequest build();
  }

  static Builder builder() {
    return new AutoValue_DefaultFullRequest.Builder();
  }
}
