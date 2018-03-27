package com.xjeffrose.xio.tracing;

import brave.http.HttpServerAdapter;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http2.Http2Headers;
import javax.annotation.ParametersAreNonnullByDefault;
import zipkin.Endpoint;

@ParametersAreNonnullByDefault
class XioHttp2ServerAdapter extends HttpServerAdapter<Http2Headers, Http2Headers> {

  private final boolean ssl;

  private StringBuilder newBuilder() {
    if (ssl) {
      return new StringBuilder("https://");
    } else {
      return new StringBuilder("http://");
    }
  }

  public XioHttp2ServerAdapter(boolean ssl) {
    this.ssl = ssl;
  }

  @Override
  public String method(Http2Headers headers) {
    return headers.method().toString();
  }

  @Override
  public String url(Http2Headers headers) {
    StringBuilder url =
        newBuilder().append(headers.get(HttpHeaderNames.HOST)).append(headers.path());
    return url.toString();
  }

  @Override
  public String requestHeader(Http2Headers request, String name) {
    return request.get(name).toString();
  }

  @Override
  public Integer statusCode(Http2Headers response) {
    if (response.status() != null) {
      // todo: WBK - what is the status here?
      return Integer.parseInt(response.status().toString());
    } else {
      return null;
    }
  }

  @Override
  public boolean parseClientAddress(Http2Headers request, Endpoint.Builder builder) {
    if (super.parseClientAddress(request, builder)) {
      return true;
    }
    CharSequence remoteIp = request.get("x-remote-ip");
    return remoteIp != null && builder.parseIp(remoteIp.toString());
  }
}
