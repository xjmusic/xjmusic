// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.resource;

import io.xj.core.CoreModule;
import io.xj.core.app.Health;
import io.xj.core.transport.HttpResponseProvider;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 This resource (along with everything in this `core/resource` package)
 is imported by all JAX-RS resources (e.g. in the Hub or Worker app),
 and results in an /o2 healthcheck route made available in all apps.
 <p>
 Due to Jersey JAX-RS implementing an HK2 injection vector,
 we cannot use Guice injection at the class level.
 That's why it appears inside this class as a manual inject.getInstance(...for each class...)
 <p>
 FUTURE: determine a more testable injection vector
 */
@Path("o2")
public class HealthcheckResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  //  private static final Logger log = LoggerFactory.getLogger(HealthcheckResource.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);
  private final Health health = injector.getInstance(Health.class);

  /**
   Method handling HTTP GET requests. The returned object will be sent
   to the client as "text/plain" media type.

   @return String that will be returned as a text/plain response.
   */
  @GET
  @Context
  @PermitAll
  public Response healthcheck() {
    try {
      health.check();
    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
    return Response
      .accepted()
      .status(Response.Status.OK)
      .build();
  }
}
