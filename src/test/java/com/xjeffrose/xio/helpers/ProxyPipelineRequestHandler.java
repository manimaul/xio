package com.xjeffrose.xio.helpers;

import com.xjeffrose.xio.application.ApplicationState;
import com.xjeffrose.xio.client.ChannelConfiguration;
import com.xjeffrose.xio.server.XioServerConfig;
import com.xjeffrose.xio.server.XioServerState;
import com.xjeffrose.xio.http.Http1ClientCodec;
import com.xjeffrose.xio.http.PipelineRequestHandler;
import com.xjeffrose.xio.http.Request;
import com.xjeffrose.xio.http.Route;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Queue;
import java.util.ArrayDeque;
import io.netty.handler.codec.http.HttpClientCodec;
import com.xjeffrose.xio.http.RawBackendHandler;
import io.netty.handler.ssl.SslContext;
import com.xjeffrose.xio.SSL.SslContextFactory;
import com.xjeffrose.xio.client.ClientConfig;

public class ProxyPipelineRequestHandler implements PipelineRequestHandler {

  InetSocketAddress remote;
  Channel channel;
  private final Queue<Request> writes = new ArrayDeque<Request>();
  boolean enableTls;

  public ProxyPipelineRequestHandler(InetSocketAddress remote, boolean enableTls) {
    this.remote = remote;
    this.enableTls = enableTls;
  }

  private ChannelFuture connect(ChannelHandlerContext ctx) {
    ChannelConfiguration config = ChannelConfiguration.clientConfig(ctx.channel().eventLoop());

    Bootstrap b = new Bootstrap();
    b.channel(config.channel());
    b.group(config.workerGroup());
    b.handler(new ChannelInitializer() {
      public void initChannel(Channel channel) {
        if (enableTls) {
          ClientConfig config = ClientConfig.fromConfig("xio.h1TestClient");

          SslContext sslContext = SslContextFactory.buildClientContext(config.getTls());

          channel.pipeline().addLast(sslContext.newHandler(channel.alloc(), remote.getHostString(), remote.getPort()));
        }

        channel.pipeline()
          .addLast(new HttpClientCodec())
          .addLast(new Http1ClientCodec())
          .addLast(new RawBackendHandler(ctx))
          ;
      }
    });
    return b.connect(remote);
  }

  public void handle(ChannelHandlerContext ctx, Request request, Route route) {
    if (channel == null) {
      // queue writes
      writes.add(request);
      final ChannelFuture future = connect(ctx);
      future.addListener(f -> {
        if (f.isDone() && f.isSuccess()) {
          channel = future.channel();
          for (Request r : writes) {
            channel.write(r);
          }
          writes.clear();
          channel.flush();
        } else {
          throw new RuntimeException(f.cause());
        }
      });
    } else {
      channel.writeAndFlush(request);
    }
  }

}
