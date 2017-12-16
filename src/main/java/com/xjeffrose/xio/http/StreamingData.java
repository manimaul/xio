package com.xjeffrose.xio.http;

import com.xjeffrose.xio.core.internal.UnstableApi;
import io.netty.buffer.ByteBuf;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

@UnstableApi
public interface StreamingData {

  ByteBuf content();

  boolean endOfStream();

  Headers trailingHeaders();

}
