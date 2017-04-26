// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.chain_config;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.ChainConfigDAO;
import io.outright.xj.core.model.chain_config.ChainConfig;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.jooq.types.ULong;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Chain record
 */
@Path("chain-configs/{id}")
public class ChainConfigRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
//  private static Logger log = LoggerFactory.getLogger(ChainConfigRecordResource.class);
  private final ChainConfigDAO chainConfigDAO = injector.getInstance(ChainConfigDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   * Get one ChainConfig by id
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);
    try {
      JSONObject result = chainConfigDAO.readOne(access, ULong.valueOf(id));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(ChainConfig.KEY_ONE, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return httpResponseProvider.notFound("Chain config");
      }

    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
  }

  /**
   * Delete one ChainConfig
   *
   * @return application/json response.
   */
  // TODO [hub] Return 404 if the chain config is not found.
  @DELETE
  @RolesAllowed({Role.ADMIN})
  public Response delete(@Context ContainerRequestContext crc) {
    AccessControl access = AccessControl.fromContext(crc);
    try {
      chainConfigDAO.delete(access, ULong.valueOf(id));
      return Response.accepted("{}").build();
    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
  }

}
