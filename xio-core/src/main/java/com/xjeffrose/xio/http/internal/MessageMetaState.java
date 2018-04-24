package com.xjeffrose.xio.http.internal;

import com.xjeffrose.xio.http.Request;
import com.xjeffrose.xio.http.Response;

public class MessageMetaState {
  public final Request request;
  public Response response;
  public boolean requestFinished;
  public boolean responseFinished;

  public MessageMetaState(Request request, boolean requestFinished) {
    this.request = request;
    this.requestFinished = requestFinished;
    this.responseFinished = false;
  }
}
