// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.chain_config;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.config.Exposure;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.ChainConfigDAO;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.model.chain_config.ChainConfig;
import io.outright.xj.core.model.chain_config.ChainConfigWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 Chain record
 */
@Path("chain-configs")
public class ChainConfigIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  //  private static Logger log = LoggerFactory.getLogger(ChainConfigIndexResource.class);
  private final ChainConfigDAO chainConfigDAO = injector.getInstance(ChainConfigDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("chainId")
  String chainId;

  /**
   Get Configs in one chain.

   @return application/json response.
   */
  // TODO [hub] Return 404 if the chain is not found.
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);

    if (chainId == null || chainId.length() == 0) {
      return httpResponseProvider.notAcceptable("Chain id is required");
    }

    try {
      JSONArray result = chainConfigDAO.readAllIn(access, ULong.valueOf(chainId));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(ChainConfig.KEY_MANY, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return Response.noContent().build();
      }

    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
  }

  /**
   Create new chain config

   @param data with which to update Chain record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ADMIN})
  public Response create(ChainConfigWrapper data, @Context ContainerRequestContext crc) {
    AccessControl access = AccessControl.fromContext(crc);
    try {
      JSONObject result = chainConfigDAO.create(access, data);
      return Response
        .created(Exposure.apiURI(ChainConfig.KEY_MANY + "/" + result.get(Entity.KEY_ID)))
        .entity(JSON.wrap(ChainConfig.KEY_ONE, result).toString())
        .build();

    } catch (Exception e) {
      return httpResponseProvider.failureToCreate(e);
    }
  }

}
