package com.test.integration.backend

import com.typesafe.config.ConfigFactory
import com.xjeffrose.xio.SSL.TlsConfig
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType.Application.Json
import io.ktor.request.httpMethod
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.jetty.Jetty
import io.ktor.server.netty.Netty
import org.slf4j.LoggerFactory
import java.security.KeyStore

private val log = LoggerFactory.getLogger(Main::class.java)

private const val responseJson = """{"title":"Release","description":"the Kraken"}"""

fun Application.main(name: String) {
  install(DefaultHeaders)
  install(CallLogging)
  routing {
    get("/") {
      val echo = call.request.headers["x-echo"] ?: "none"
      val method = call.request.httpMethod.value
      call.response.headers.apply {
        append("x-tag", name)
        append("x-method", method)
        append("x-echo", echo)
      }
      call.respondText(responseJson, Json)
    }
    post("/") {
      val echo = call.request.headers["x-echo"] ?: "none"
      val method = call.request.httpMethod.value
      call.response.headers.apply {
        append("x-tag", name)
        append("x-method", method)
        append("x-echo", echo)
      }
      call.respondText(responseJson, Json)
    }
  }
}


object Main {
  @Throws(Exception::class)
  @JvmStatic
  fun main(args: Array<String>) {
    if (args.size != 3) {
      throw RuntimeException("please supply port, name and h2 capable")
    }
    val portNo = args[0].toInt()
    val name = args[1]
    val h2Capable = args[2].toBoolean()

    log.warn("starting h2 capable:$h2Capable service:$name on port:$portNo")

    val tlsConfig = TlsConfig(ConfigFactory.load())
    val jks = KeyStore.getInstance("JKS")
    jks.load(null, null)
    jks.setCertificateEntry("mykey", tlsConfig.certificateAndChain.first())
    jks.setKeyEntry("mykey", tlsConfig.privateKey, "changeit".toCharArray(), tlsConfig.certificateAndChain)

    val env = applicationEngineEnvironment {
      module {
        main(name)
      }
      sslConnector(
        keyStore = jks,
        keyAlias = "mykey",
        keyStorePassword = {
          "changeit".toCharArray()
        },
        privateKeyPassword = {
          "changeit".toCharArray()
        },
        builder = {
          port = portNo
        }
      )
    }
    embeddedServer(if (h2Capable) Netty else Jetty, env).start(wait = true)
  }
}
