package io.outright.xj.core.application

import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.jersey.filter.LoggingFilter
import org.glassfish.jersey.server.ResourceConfig

import static org.mockito.Matchers.any
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.doThrow
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
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
    System.setProperty("log.access.entities.maxsize", "5")
    ApplicationImpl appSpy = spy(new ApplicationImpl(
      mockPackages,
      8002
    ))

    ResourceConfig mockResourceConfig = mock(ResourceConfig.class)
    doReturn(mockResourceConfig).when(appSpy).newResourceConfig(expectPackages)

    HttpServer mockHttpServer = mock(HttpServer.class)
    doReturn(mockHttpServer).when(appSpy).createHttpServer(mockResourceConfig)

    LoggingFilter mockLoggingFilter = mock(LoggingFilter.class)
    doReturn(mockLoggingFilter).when(appSpy).newFileLoggingFilter(tempFile.getAbsolutePath(), 5)

    appSpy.Start()

    System.clearProperty("log.access.entities.maxsize")
    verify(appSpy).newResourceConfig(expectPackages)
    verify(appSpy).newFileLoggingFilter(tempFile.getAbsolutePath(), 5)
    verify(mockResourceConfig).register(mockLoggingFilter)
  }

  void testStart_AccessLogEntitiesAll() {
    System.setProperty("log.access.entities.all", "false")
    ApplicationImpl appSpy = spy(new ApplicationImpl(
      mockPackages,
      8002
    ))

    ResourceConfig mockResourceConfig = mock(ResourceConfig.class)
    doReturn(mockResourceConfig).when(appSpy).newResourceConfig(expectPackages)

    HttpServer mockHttpServer = mock(HttpServer.class)
    doReturn(mockHttpServer).when(appSpy).createHttpServer(mockResourceConfig)

    LoggingFilter mockLoggingFilter = mock(LoggingFilter.class)
    doReturn(mockLoggingFilter).when(appSpy).newFileLoggingFilter(tempFile.getAbsolutePath(), false)

    appSpy.Start()

    System.clearProperty("log.access.entities.all")
    verify(appSpy).newResourceConfig(expectPackages)
    verify(appSpy).newFileLoggingFilter(tempFile.getAbsolutePath(), false)
    verify(mockResourceConfig).register(mockLoggingFilter)
  }

  void testStart_ThrowsErrorOpeningAccessLog() {
    System.setProperty("log.access.entities.maxsize", "5")
    ApplicationImpl appSpy = spy(new ApplicationImpl(
      mockPackages,
      8002
    ))

    ResourceConfig mockResourceConfig = mock(ResourceConfig.class)
    doReturn(mockResourceConfig).when(appSpy).newResourceConfig(expectPackages)

    HttpServer mockHttpServer = mock(HttpServer.class)
    doReturn(mockHttpServer).when(appSpy).createHttpServer(mockResourceConfig)

    doThrow(new IOException()).when(appSpy).newFileLoggingFilter(tempFile.getAbsolutePath(), 5)

    appSpy.Start()

    System.clearProperty("log.access.entities.maxsize")
    verify(appSpy).newResourceConfig(expectPackages)
    verify(appSpy).newFileLoggingFilter(tempFile.getAbsolutePath(), 5)
  }

  void testStop() {
    ApplicationImpl appSpy = spy(new ApplicationImpl(
      mockPackages,
      8002
    ))

    HttpServer mockHttpServer = mock(HttpServer.class)
    doReturn(mockHttpServer).when(appSpy).getHttpServer()

    appSpy.Stop()

    verify(appSpy).getHttpServer()
    verify(mockHttpServer).shutdownNow()
  }

  void testBaseURI_Default() {
    assert new ApplicationImpl(
      mockPackages,
      8002
    ).BaseURI() == "http://0.0.0.0:8002/"
  }

  void testBaseURI_CustomHost() {
    System.setProperty("app.host","special")
    assert new ApplicationImpl(
      mockPackages,
      8002
    ).BaseURI() == "http://special:8002/"
    System.clearProperty("app.host")
  }
}
