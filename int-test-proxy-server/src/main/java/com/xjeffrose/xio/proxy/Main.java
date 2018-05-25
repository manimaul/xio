package com.xjeffrose.xio.proxy;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      throw new RuntimeException("please specify a name and config path");
    }
    log.debug("starting proxy server {}", args[0]);
    String configPath = args[1];
    Config config = ConfigFactory.load(ConfigFactory.parseFile(new File(configPath)));
    new ReverseProxyServer("xio.reverseProxy").start(config);
    log.debug("proxy accepting connections");
  }
}
