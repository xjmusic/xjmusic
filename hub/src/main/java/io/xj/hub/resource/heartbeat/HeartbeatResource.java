// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.hub.resource.heartbeat;

import io.xj.core.CoreModule;
import io.xj.core.config.Config;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.work.Work;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.work.WorkManager;

import com.google.inject.Guice;
import com.google.inject.Injector;

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
public class HeartbeatResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);
  private final WorkManager workManager = injector.getInstance(WorkManager.class);

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
    if (Objects.isNull(key) || !Objects.equals(key, Config.platformHeartbeatKey())) {
      return response.unauthorized();
    }

    try {
      return response.readMany(
        Work.KEY_MANY,
        workManager.reinstateAllWork());

    } catch (Exception e) {
      return response.failure(e);
    }
  }
}
