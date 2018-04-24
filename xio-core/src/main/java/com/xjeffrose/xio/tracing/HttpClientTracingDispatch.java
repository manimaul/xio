package com.xjeffrose.xio.tracing;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.http.HttpClientHandler;
import brave.http.HttpTracing;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import com.xjeffrose.xio.http.*;
import io.netty.channel.ChannelHandlerContext;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import lombok.Getter;
import lombok.val;

public class HttpClientTracingDispatch extends HttpTracingState {

  @Getter private final Tracing tracing;
  @Getter private final Tracer tracer;
  @Getter private final HttpClientHandler<Request, Response> handler;
  @Getter private final TraceContext.Injector<Headers> injector;

  public HttpClientTracingDispatch(HttpTracing httpTracing, boolean ssl) {
    tracing = httpTracing.tracing();
    tracer = tracing.tracer();
    handler = HttpClientHandler.create(httpTracing, new XioHttpClientAdapter(ssl));
    injector = httpTracing.tracing().propagation().injector(Headers::setLowerCase);
  }

  private Headers addRemoteIp(ChannelHandlerContext ctx, Headers headers) {
    SocketAddress address = ctx.channel().remoteAddress();
    if (address instanceof InetSocketAddress) {
      headers.set("x-remote-ip", ((InetSocketAddress) address).getHostString());
    }
    return headers;
  }

  public void onRequest(ChannelHandlerContext ctx, Request request) {
    val scope =
        request
            .httpTraceInfo()
            .getSpan()
            .map(span -> tracing.currentTraceContext().newScope(span.context()));

    Span span = handler.handleSend(injector, addRemoteIp(ctx, request.headers()), request);
    request.httpTraceInfo().setSpan(span);

    scope.ifPresent(CurrentTraceContext.Scope::close);
  }

  public void onResponse(ChannelHandlerContext ctx, Response response) {
    HttpMessageSession.currentRequestSpan(ctx, response.streamId()).ifPresent(
        span -> {
          response.httpTraceInfo().setSpan(span);
          handler.handleReceive(response, null, span);
        });
  }

  public void onError(ChannelHandlerContext ctx, Throwable cause) {
    HttpMessageSession.allCurrentRequestSpans(ctx).forEach(span -> handler.handleReceive(null, cause, span));
  }

  public void onError(ChannelHandlerContext ctx, Message message, Throwable cause) {
    message.httpTraceInfo().getSpan().ifPresent(span -> handler.handleReceive(null, cause, span));
  }
}
