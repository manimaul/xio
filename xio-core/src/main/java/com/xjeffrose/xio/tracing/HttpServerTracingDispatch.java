package com.xjeffrose.xio.tracing;

import brave.Span;
import brave.http.HttpServerHandler;
import brave.http.HttpTracing;
import brave.propagation.TraceContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http2.Http2Headers;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class HttpServerTracingDispatch {

  private final HttpServerHandler<HttpRequest, HttpResponse> http1Handler;
  private final HttpServerHandler<Http2Headers, Http2Headers> http2Handler;

  private final TraceContext.Extractor<HttpHeaders> http1extractor;
  private final TraceContext.Extractor<Http2Headers> http2extractor;

  public HttpServerTracingDispatch(HttpTracing httpTracing, boolean ssl) {
    http1Handler = HttpServerHandler.create(httpTracing, new XioHttpServerAdapter(ssl));
    http2Handler = HttpServerHandler.create(httpTracing, new XioHttp2ServerAdapter(ssl));

    http1extractor = httpTracing.tracing().propagation().extractor(HttpHeaders::get);
    http2extractor =
        httpTracing.tracing().propagation().extractor((header, key) -> header.get(key).toString());
  }

  private HttpHeaders addRemoteIp(ChannelHandlerContext ctx, HttpHeaders headers) {
    SocketAddress address = ctx.channel().remoteAddress();
    if (address instanceof InetSocketAddress) {
      headers.set("x-remote-ip", ((InetSocketAddress) address).getHostString());
    }
    return headers;
  }

  private Http2Headers addRemoteIp(ChannelHandlerContext ctx, Http2Headers headers) {
    SocketAddress address = ctx.channel().remoteAddress();
    if (address instanceof InetSocketAddress) {
      headers.set("x-remote-ip", ((InetSocketAddress) address).getHostString());
    }
    return headers;
  }

  public Span onRequest(ChannelHandlerContext ctx, Http2Headers headers) {
    return http2Handler.handleReceive(http2extractor, addRemoteIp(ctx, headers), headers);
  }

  public Span onRequest(ChannelHandlerContext ctx, HttpRequest request) {
    return http1Handler.handleReceive(http1extractor, addRemoteIp(ctx, request.headers()), request);
  }

  public void onResponse(HttpResponse response, Span span, @Nullable Throwable error) {
    http1Handler.handleSend(response, error, span);
  }

  public void onResponse(Http2Headers response, Span span, @Nullable Throwable error) {
    http2Handler.handleSend(response, error, span);
  }
}
