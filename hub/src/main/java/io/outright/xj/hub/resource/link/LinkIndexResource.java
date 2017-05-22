// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.link;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.ChoiceDAO;
import io.outright.xj.core.dao.LinkChordDAO;
import io.outright.xj.core.dao.LinkDAO;
import io.outright.xj.core.dao.LinkMemeDAO;
import io.outright.xj.core.dao.LinkMessageDAO;
import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.model.ChordEntity;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.link_chord.LinkChord;
import io.outright.xj.core.model.link_meme.LinkMeme;
import io.outright.xj.core.model.link_message.LinkMessage;
import io.outright.xj.core.model.MemeEntity;
import io.outright.xj.core.model.message.Message;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;

import org.jooq.types.ULong;

import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;

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
import java.util.Map;

/**
 Links
 */
@Path("links")
public class LinkIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final ChoiceDAO choiceDAO = injector.getInstance(ChoiceDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);
  private final LinkChordDAO linkChordDAO = injector.getInstance(LinkChordDAO.class);
  private final LinkDAO linkDAO = injector.getInstance(LinkDAO.class);
  private final LinkMemeDAO linkMemeDAO = injector.getInstance(LinkMemeDAO.class);
  private final LinkMessageDAO linkMessageDAO = injector.getInstance(LinkMessageDAO.class);

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
      return Response
        .accepted(JSON.wrap(readAllIncludingRelationships(Access.fromContext(crc))).toString())
        .type(MediaType.APPLICATION_JSON)
        .build();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Read all links, including reading relations request in the `?include=` query parameter

   @param access control
   @return map of entity plural key to array of entities
   @throws Exception on failure
   */
  private Map<String, JSONArray> readAllIncludingRelationships(Access access) throws Exception {
    Map<String, JSONArray> out = Maps.newHashMap();

    out.put(Link.KEY_MANY, JSON.arrayOf(linkDAO.readAll(access, ULong.valueOf(chainId))));

    if (include.contains(Message.KEY_MANY))
      out.put(LinkMessage.KEY_MANY, JSON.arrayOf(linkMessageDAO.readAllInChain(access, ULong.valueOf(chainId))));

    if (include.contains(MemeEntity.KEY_MANY))
      out.put(LinkMeme.KEY_MANY, JSON.arrayOf(linkMemeDAO.readAllInChain(access, ULong.valueOf(chainId))));

    if (include.contains(ChordEntity.KEY_MANY))
      out.put(LinkChord.KEY_MANY, JSON.arrayOf(linkChordDAO.readAllInChain(access, ULong.valueOf(chainId))));

    if (include.contains(Choice.KEY_MANY))
      out.put(Choice.KEY_MANY, JSON.arrayOf(choiceDAO.readAllInChain(access, ULong.valueOf(chainId))));

    return out;
  }

}
