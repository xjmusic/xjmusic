// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.heartbeat;

import io.xj.core.app.Heartbeat;
import io.xj.core.config.Config;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.work.Work;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.core.work.WorkManager;
import io.xj.hub.HubResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.jws.WebResult;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Objects;

/**
 Platform heartbeat is called from outside the platform, exactly once per X.
 */
@Path("heartbeat")
public class HeartbeatResource extends HubResource {
  private final Logger log = LoggerFactory.getLogger(HeartbeatResource.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);
  private final Heartbeat heartbeat = injector.getInstance(Heartbeat.class);

  @FormParam("key")
  String key;

  /**
   Do one heartbeat (public, protected by key)

   @return application/json response.
   */
  @POST
  @WebResult
  @PermitAll
  public Response getConfig(@Context ContainerRequestContext crc) throws IOException, ConfigException {
    if (Objects.isNull(key)) {
      log.warn("heartbeat without key");
      return response.notAcceptable("authorization required");
    }

    if (!Objects.equals(key, Config.platformHeartbeatKey())) {
      log.warn("heartbeat with incorrect key, expect:{}, actual:{}", Config.platformHeartbeatKey(), key);
      return response.unauthorized();
    }

    try {
      return response.readOne(
        Heartbeat.KEY_ONE,
        heartbeat.pulse());

    } catch (Exception e) {
      return response.failure(e);
    }
  }
}
