// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.rest_api.Payload;
import io.xj.lib.rest_api.RestApiException;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.dao.ChainDAO;
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
public class HeartbeatEndpoint extends HubEndpoint {
  private final Logger log = LoggerFactory.getLogger(HeartbeatEndpoint.class);
  private final ChainDAO chainDAO;
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
    config = injector.getInstance(Config.class);
    chainDAO = injector.getInstance(ChainDAO.class);
    ;
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
      Payload payload = new Payload();
      workManager.reinstateAllWork().forEach(work -> {
        try {
          payload.addData(payloadFactory.toPayloadObject(work));
        } catch (RestApiException e) {
          log.error("Failed to reinstate work {}", work, e);
          payload.addError(payloadFactory.newPayloadError()
            .setTitle("HeartbeatFailure")
            .setDetail(String.format("Failed to revive work[id=%s]: %s", work.getId(), e.getMessage())));
        }
      });
      chainDAO.checkAndReviveAll(Access.internal()).forEach(chain -> {
        try {
          payload.addData(payloadFactory.toPayloadObject(chain));
        } catch (RestApiException e) {
          log.error("Failed to check and revive chain {}", chain, e);
          payload.addError(payloadFactory.newPayloadError()
            .setTitle("HeartbeatFailure")
            .setDetail(String.format("Failed to check and revive chain[id=%s]: %s", chain.getId(), e.getMessage())));
        }
      });
      return response.ok(payload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }
}
