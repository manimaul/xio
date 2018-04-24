package com.xjeffrose.xio.http;

import brave.Span;
import io.netty.channel.ChannelHandlerContext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HttpMessageSession {

  public static Optional<Span> currentRequestSpan(ChannelHandlerContext ctx, int streamId) {
    final Optional<Request> optionalRequest;
    if (streamId == Message.H1_STREAM_ID_NONE) {
      optionalRequest = Optional.ofNullable(Http1MessageSession.lazyCreateSession(ctx).currentRequest());
    } else {
      optionalRequest = Optional.ofNullable(Http2MessageSession.lazyCreateSession(ctx).currentRequest(streamId));
    }
    return optionalRequest.flatMap(request -> request.httpTraceInfo().getSpan());
  }

  public static List<Span> allCurrentRequestSpans(ChannelHandlerContext ctx) {
    Request h1Request = Http1MessageSession.lazyCreateSession(ctx).currentRequest();
    if (h1Request != null) {
      return h1Request.httpTraceInfo().getSpan()
        .map(Collections::singletonList)
        .orElse(Collections.emptyList());
    } else {
      return Http2MessageSession.lazyCreateSession(ctx).allCurrentRequests()
        .map(request -> request.httpTraceInfo().getSpan())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
    }
  }
}
