package com.xjeffrose.xio.pipeline;

import static org.mockito.Mockito.*;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.xjeffrose.xio.application.ApplicationConfig;
import com.xjeffrose.xio.application.ApplicationState;
import com.xjeffrose.xio.core.ConnectionContextHandler;
import com.xjeffrose.xio.core.XioExceptionLogger;
import com.xjeffrose.xio.core.XioMessageLogger;
import com.xjeffrose.xio.core.XioNoOpHandler;
import com.xjeffrose.xio.filter.Http1Filter;
import com.xjeffrose.xio.filter.IpFilter;
import com.xjeffrose.xio.server.XioBehavioralRuleEngine;
import com.xjeffrose.xio.server.XioConnectionLimiter;
import com.xjeffrose.xio.server.XioResponseClassifier;
import com.xjeffrose.xio.server.XioServerConfig;
import com.xjeffrose.xio.server.XioServerLimits;
import com.xjeffrose.xio.server.XioServerState;
import com.xjeffrose.xio.server.XioWebApplicationFirewall;
import com.xjeffrose.xio.tracing.HttpServerTracingHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import org.junit.Test;
import org.mockito.InOrder;

public class XioServerPipelineUnitTest {

  @Test
  public void verifyHandlers() {
    Config config = ConfigFactory.load();
    ApplicationState appState =
        new ApplicationState(ApplicationConfig.fromConfig("xio.testApplication", config));
    XioServerConfig serverConfig =
        XioServerConfig.fromConfig("xio.testApplication.servers.testServer");
    XioServerState serverState = new XioServerState(serverConfig);

    // Build class under test
    XioServerPipeline server =
        new XioServerPipeline() {
          @Override
          public ChannelHandler getEncryptionHandler(XioServerConfig config, XioServerState state) {
            return new XioNoOpHandler();
          }

          @Override
          public ChannelHandler getIdleDisconnectHandler(XioServerLimits limits) {
            return new XioNoOpHandler();
          }

          @Override
          public ChannelHandler getTlsAuthenticationHandler() {
            return new XioNoOpHandler();
          }

          @Override
          public ChannelHandler getAuthenticationHandler() {
            return new XioNoOpHandler();
          }

          @Override
          public ChannelHandler getAuthorizationHandler() {
            return new XioNoOpHandler();
          }

          @Override
          public ChannelHandler getCodecNegotiationHandler(XioServerConfig config) {
            return new XioNoOpHandler();
          }

          @Override
          public ChannelHandler getCodecHandler(XioServerConfig config) {
            return new XioNoOpHandler();
          }

          @Override
          public ChannelHandler getApplicationCodec() {
            return new XioNoOpHandler();
          }

          @Override
          public ChannelHandler getApplicationRouter() {
            return new XioNoOpHandler();
          }

          @Override
          public ChannelHandler getApplicationHandler() {
            return new XioNoOpHandler();
          }
        };
    ChannelPipeline pipeline = mock(ChannelPipeline.class);
    server.buildHandlers(appState, serverConfig, serverState, pipeline);
    InOrder inOrder = inOrder(pipeline);
    inOrder
        .verify(pipeline, times(1))
        .addLast(eq("globalConnectionLimiter"), isA(XioConnectionLimiter.class));
    inOrder
        .verify(pipeline, times(1))
        .addLast(eq("serviceConnectionLimiter"), isA(XioConnectionLimiter.class));
    inOrder
        .verify(pipeline, times(1))
        .addLast(eq("idleDisconnectHandler"), isA(XioNoOpHandler.class));
    inOrder
        .verify(pipeline, times(1))
        .addLast(eq("l4DeterministicRuleEngine"), isA(IpFilter.class));
    inOrder
        .verify(pipeline, times(1))
        .addLast(eq("l4BehavioralRuleEngine"), isA(XioBehavioralRuleEngine.class));
    inOrder
        .verify(pipeline, times(1))
        .addLast(eq("connectionContext"), isA(ConnectionContextHandler.class));
    inOrder
        .verify(pipeline, times(1))
        .addLast(eq("globalChannelStatistics"), eq(serverState.getChannelStatistics()));
    inOrder.verify(pipeline, times(1)).addLast(eq("encryptionHandler"), isA(XioNoOpHandler.class));
    inOrder
        .verify(pipeline, times(1))
        .addLast(eq("tls authentication handler"), isA(XioNoOpHandler.class));
    inOrder.verify(pipeline, times(1)).addLast(eq("messageLogger"), isA(XioMessageLogger.class));
    inOrder.verify(pipeline, times(1)).addLast(eq("codecNegotiation"), isA(XioNoOpHandler.class));
    inOrder.verify(pipeline, times(1)).addLast(eq("codec"), isA(XioNoOpHandler.class));
    inOrder.verify(pipeline, times(1)).addLast(eq("application codec"), isA(XioNoOpHandler.class));
    inOrder
        .verify(pipeline, times(1))
        .addLast(eq("distributed tracing"), isA(HttpServerTracingHandler.class));
    inOrder.verify(pipeline, times(1)).addLast(eq("application router"), isA(XioNoOpHandler.class));
    inOrder
        .verify(pipeline, times(1))
        .addLast(eq("authentication handler"), isA(XioNoOpHandler.class));
    inOrder
        .verify(pipeline, times(1))
        .addLast(eq("l7DeterministicRuleEngine"), isA(Http1Filter.class));
    inOrder
        .verify(pipeline, times(1))
        .addLast(eq("l7BehavioralRuleEngine"), isA(XioBehavioralRuleEngine.class));
    inOrder
        .verify(pipeline, times(1))
        .addLast(eq("webApplicationFirewall"), isA(XioWebApplicationFirewall.class));
    inOrder
        .verify(pipeline, times(1))
        .addLast(eq("authorization handler"), isA(XioNoOpHandler.class));
    inOrder
        .verify(pipeline, times(1))
        .addLast(eq("xioResponseClassifier"), isA(XioResponseClassifier.class));
    inOrder
        .verify(pipeline, times(1))
        .addLast(eq("exceptionLogger"), isA(XioExceptionLogger.class));
    inOrder.verify(pipeline, times(1)).addLast(eq("applicationHandler"), isA(XioNoOpHandler.class));
  }
}
