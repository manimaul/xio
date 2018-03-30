package com.xjeffrose.xio.server;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.xjeffrose.xio.SSL.TlsConfig;
import com.xjeffrose.xio.tracing.XioTracing;
import io.netty.channel.ChannelOption;
import java.net.InetSocketAddress;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

// TODO(CK): rename this to ServerConfig
@Slf4j
public class XioServerConfig {
  @Getter private final Map<ChannelOption<Object>, Object> bootstrapOptions;
  @Getter private String name;
  @Getter private InetSocketAddress bindAddress;
  @Getter private XioServerLimits limits;
  @Getter private TlsConfig tls;
  @Getter private final boolean messageLoggerEnabled;
  @Getter private final XioTracing tracing;

  public XioServerConfig(Config config) {
    this(config, new XioTracing(config));
  }

  public XioServerConfig(Config config, XioTracing tracing) {
    bootstrapOptions = null;
    name = config.getString("name");
    String address;
    if (config.hasPath("settings.bindHost")) {
      address = config.getString("settings.bindHost");
      log.warn("settings.bindHost is deprecated please use settings.bindIp");
    } else {
      address = config.getString("settings.bindIp");
    }

    bindAddress = new InetSocketAddress(address, config.getInt("settings.bindPort"));
    limits = new XioServerLimits(config.getConfig("limits"));
    tls = new TlsConfig(config.getConfig("settings.tls"));
    messageLoggerEnabled = config.getBoolean("settings.messageLoggerEnabled");
    if (!tls.isUseSsl() && tls.isLogInsecureConfig()) {
      log.warn("Server '{}' has useSsl set to false!", name);
    }
    this.tracing = tracing;
  }

  public static XioServerConfig fromConfig(String key, Config config, XioTracing tracing) {
    return new XioServerConfig(config.getConfig(key), tracing);
  }

  public static XioServerConfig fromConfig(String key, Config config) {
    return new XioServerConfig(config.getConfig(key));
  }

  public static XioServerConfig fromConfig(String key) {
    return fromConfig(key, ConfigFactory.load());
  }

  public boolean isTlsEnabled() {
    return tls.isUseSsl();
  }
}
