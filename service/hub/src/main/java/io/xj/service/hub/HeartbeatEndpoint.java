// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub;

import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.core.app.AppResource;
import io.xj.lib.core.heartbeat.Heartbeat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Objects;

/**
 Platform heartbeat is called of outside the platform, exactly once per X.
 */
@Path("heartbeat")
public class HeartbeatEndpoint extends AppResource {
  private final Logger log = LoggerFactory.getLogger(HeartbeatEndpoint.class);
  private Heartbeat heartbeat;
  private Config config;


  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public HeartbeatEndpoint(
    Injector injector
  ) {
    super(injector);
    heartbeat = injector.getInstance(Heartbeat.class);
    config = injector.getInstance(Config.class);
  }

  /**
   Do one heartbeat (public, protected by key)

   @return application/json response.
   */
  @POST
  @PermitAll
  public Response getConfig(@Context ContainerRequestContext crc, @FormParam("key") String key) {
    if (Objects.isNull(key)) {
      log.warn("heartbeat without key");
      return response.notAcceptable("authorization required");
    }

    String correctKey = config.getString("platform.heartbeatKey");

    if (!Objects.equals(key, correctKey)) {
      log.warn("heartbeat with incorrect key, expect:{}, actual:{}", correctKey, key);
      return response.unauthorized();
    }

    try {
      return response.ok(heartbeat.pulse());

    } catch (Exception e) {
      return response.failure(e);
    }
  }
}
