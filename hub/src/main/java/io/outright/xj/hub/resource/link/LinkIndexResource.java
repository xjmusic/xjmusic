// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.link;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.LinkDAO;
import io.outright.xj.core.dao.LinkMessageDAO;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.link_message.LinkMessage;
import io.outright.xj.core.model.message.Message;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;

import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 Links
 */
@Path("links")
public class LinkIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final LinkDAO linkDAO = injector.getInstance(LinkDAO.class);
  private final LinkMessageDAO linkMessageDAO = injector.getInstance(LinkMessageDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("chainId")
  String chainId;

  @QueryParam("include")
  String include;

  /**
   Get all links.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (chainId == null || chainId.length() == 0) {
      return response.notAcceptable("Chain id is required");
    }

    try {
      if (include.contains(Message.KEY_MANY))
        return Response
          .accepted(JSON.wrap(ImmutableMap.of(
            Link.KEY_MANY, JSON.arrayOf(linkDAO.readAll(Access.fromContext(crc), ULong.valueOf(chainId))),
            LinkMessage.KEY_MANY, JSON.arrayOf(linkMessageDAO.readAllInChain(Access.fromContext(crc), ULong.valueOf(chainId)))
          )).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();

      else
        return response.readMany(
          Link.KEY_MANY,
          linkDAO.readAll(
            Access.fromContext(crc),
            ULong.valueOf(chainId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
