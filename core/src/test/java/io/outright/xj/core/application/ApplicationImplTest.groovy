package io.outright.xj.core.application

import io.outright.xj.core.application.server.HttpServerFactory
import io.outright.xj.core.application.server.LogFilterFactory
import io.outright.xj.core.application.server.ResourceConfigFactory
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.jersey.filter.LoggingFilter
import org.glassfish.jersey.server.ResourceConfig

import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.doThrow
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

class ApplicationImplTest extends GroovyTestCase {
  private static String[] mockPackages = ["one", "two", "three"]
  private static String[] expectPackages = ["io.outright.xj.core.application.resource", "one", "two", "three"]
  private File tempFile

  void setUp() throws Exception {
    super.setUp()
    try {
      tempFile = File.createTempFile("access-log-", ".tmp")
    } catch (IOException e) {
      e.printStackTrace()
    }
    System.setProperty("log.access.filename", tempFile.getAbsolutePath())
  }

  void testStart_AccessLogEntitiesMaxSize() {
    ResourceConfigFactory mockResourceConfigFactory = mock(ResourceConfigFactory.class)
    ResourceConfig mockResourceConfig = mock(ResourceConfig.class)
    doReturn(mockResourceConfig)
      .when(mockResourceConfigFactory)
      .createResourceConfig(expectPackages)

    HttpServerFactory mockHttpServerFactory = mock(HttpServerFactory.class)
    HttpServer mockHttpServer = mock(HttpServer.class)
    doReturn(mockHttpServer)
      .when(mockHttpServerFactory)
      .createHttpServer(URI.create("http://0.0.0.0:8002/"), mockResourceConfig)

    LogFilterFactory mockLogFilterFactory = mock(LogFilterFactory.class)
    LoggingFilter mockLoggingFilter = mock(LoggingFilter.class)
    doReturn(mockLoggingFilter)
      .when(mockLogFilterFactory)
      .newFilter(tempFile.getAbsolutePath(), 5)

    System.setProperty("log.access.entities.maxsize", "5")
    ApplicationImpl app = new ApplicationImpl(
      mockHttpServerFactory,
      mockResourceConfigFactory,
      mockLogFilterFactory,
      mockPackages,
      8002
    )
    app.Start()

    System.clearProperty("log.access.entities.maxsize")
    verify(mockResourceConfigFactory).createResourceConfig(expectPackages)
    verify(mockHttpServerFactory).createHttpServer(URI.create("http://0.0.0.0:8002/"), mockResourceConfig)
    verify(mockLogFilterFactory).newFilter(tempFile.getAbsolutePath(), 5)
    verify(mockResourceConfig).register(mockLoggingFilter)
    verify(mockHttpServer).start()
  }

  void testStart_AccessLogEntitiesAll() {
    ResourceConfigFactory mockResourceConfigFactory = mock(ResourceConfigFactory.class)
    ResourceConfig mockResourceConfig = mock(ResourceConfig.class)
    doReturn(mockResourceConfig)
      .when(mockResourceConfigFactory)
      .createResourceConfig(expectPackages)

    HttpServerFactory mockHttpServerFactory = mock(HttpServerFactory.class)
    HttpServer mockHttpServer = mock(HttpServer.class)
    doReturn(mockHttpServer)
      .when(mockHttpServerFactory)
      .createHttpServer(URI.create("http://0.0.0.0:8002/"), mockResourceConfig)

    LogFilterFactory mockLogFilterFactory = mock(LogFilterFactory.class)
    LoggingFilter mockLoggingFilter = mock(LoggingFilter.class)
    doReturn(mockLoggingFilter)
      .when(mockLogFilterFactory)
      .newFilter(tempFile.getAbsolutePath(), true)

    System.setProperty("log.access.entities.all", "true")
    ApplicationImpl app = new ApplicationImpl(
      mockHttpServerFactory,
      mockResourceConfigFactory,
      mockLogFilterFactory,
      mockPackages,
      8002
    )
    app.Start()

    System.clearProperty("log.access.entities.all")
    verify(mockResourceConfigFactory).createResourceConfig(expectPackages)
    verify(mockHttpServerFactory).createHttpServer(URI.create("http://0.0.0.0:8002/"), mockResourceConfig)
    verify(mockLogFilterFactory).newFilter(tempFile.getAbsolutePath(), true)
    verify(mockResourceConfig).register(mockLoggingFilter)
    verify(mockHttpServer).start()
  }

  void testStart_ThrowsErrorOpeningAccessLog() {
    ResourceConfigFactory mockResourceConfigFactory = mock(ResourceConfigFactory.class)
    ResourceConfig mockResourceConfig = mock(ResourceConfig.class)
    doReturn(mockResourceConfig)
      .when(mockResourceConfigFactory)
      .createResourceConfig(expectPackages)

    HttpServerFactory mockHttpServerFactory = mock(HttpServerFactory.class)
    HttpServer mockHttpServer = mock(HttpServer.class)
    doReturn(mockHttpServer)
      .when(mockHttpServerFactory)
      .createHttpServer(URI.create("http://0.0.0.0:8002/"), mockResourceConfig)

    LogFilterFactory mockLogFilterFactory = mock(LogFilterFactory.class)
    doThrow(new IOException("tragedy"))
      .when(mockLogFilterFactory)
      .newFilter(tempFile.getAbsolutePath(), false)

    ApplicationImpl app = new ApplicationImpl(
      mockHttpServerFactory,
      mockResourceConfigFactory,
      mockLogFilterFactory,
      mockPackages,
      8002
    )
    app.Start()

    verify(mockResourceConfigFactory).createResourceConfig(expectPackages)
    verify(mockHttpServerFactory).createHttpServer(URI.create("http://0.0.0.0:8002/"), mockResourceConfig)
    verify(mockLogFilterFactory).newFilter(tempFile.getAbsolutePath(), false)
    verify(mockHttpServer).start()
  }

  void testStop() {
    ResourceConfigFactory mockResourceConfigFactory = mock(ResourceConfigFactory.class)
    ResourceConfig mockResourceConfig = mock(ResourceConfig.class)
    doReturn(mockResourceConfig)
      .when(mockResourceConfigFactory)
      .createResourceConfig(expectPackages)

    HttpServerFactory mockHttpServerFactory = mock(HttpServerFactory.class)
    HttpServer mockHttpServer = mock(HttpServer.class)
    doReturn(mockHttpServer)
      .when(mockHttpServerFactory)
      .createHttpServer(URI.create("http://0.0.0.0:8002/"), mockResourceConfig)

    LogFilterFactory mockLogFilterFactory = mock(LogFilterFactory.class)
    LoggingFilter mockLoggingFilter = mock(LoggingFilter.class)
    doReturn(mockLoggingFilter)
      .when(mockLogFilterFactory)
      .newFilter(tempFile.getAbsolutePath(), false)

    ApplicationImpl app = new ApplicationImpl(
      mockHttpServerFactory,
      mockResourceConfigFactory,
      mockLogFilterFactory,
      mockPackages,
      8002
    )
    app.Start()
    app.Stop()

    verify(mockHttpServer).shutdownNow()
  }

  void testBaseURI_Default() {
    ResourceConfigFactory mockResourceConfigFactory = mock(ResourceConfigFactory.class)
    HttpServerFactory mockHttpServerFactory = mock(HttpServerFactory.class)
    LogFilterFactory mockLogFilterFactory = mock(LogFilterFactory.class)
    assert new ApplicationImpl(
      mockHttpServerFactory,
      mockResourceConfigFactory,
      mockLogFilterFactory,
      mockPackages,
      8002
    ).BaseURI() == "http://0.0.0.0:8002/"
  }

  void testBaseURI_CustomHost() {
    ResourceConfigFactory mockResourceConfigFactory = mock(ResourceConfigFactory.class)
    HttpServerFactory mockHttpServerFactory = mock(HttpServerFactory.class)
    LogFilterFactory mockLogFilterFactory = mock(LogFilterFactory.class)
    System.setProperty("app.host","special")
    assert new ApplicationImpl(
      mockHttpServerFactory,
      mockResourceConfigFactory,
      mockLogFilterFactory,
      mockPackages,
      8002
    ).BaseURI() == "http://special:8002/"
    System.clearProperty("app.host")
  }
}
