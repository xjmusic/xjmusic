// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.hub.resource.link;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.dao.LinkChordDAO;
import io.xj.core.dao.LinkDAO;
import io.xj.core.dao.LinkMemeDAO;
import io.xj.core.dao.LinkMessageDAO;
import io.xj.core.model.ChordEntity;
import io.xj.core.model.MemeEntity;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.link.Link;
import io.xj.core.model.link_chord.LinkChord;
import io.xj.core.model.link_meme.LinkMeme;
import io.xj.core.model.link_message.LinkMessage;
import io.xj.core.model.message.Message;
import io.xj.core.model.role.Role;
import io.xj.core.tables.records.LinkRecord;
import io.xj.core.transport.JSON;

import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.common.collect.ImmutableList;
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
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

  @QueryParam("fromOffset")
  BigInteger fromOffset;

  @QueryParam("fromSecondsUTC")
  BigInteger fromSecondsUTC;

  /**
   Get all links.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (Objects.isNull(chainId) || chainId.length() == 0)
      return response.notAcceptable("Chain id is required");

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

    Result<LinkRecord> links = readAllLinks(access);
    out.put(Link.KEY_MANY, JSON.arrayOf(links));
    List<ULong> linkIds = linkIds(links);

    if (Objects.nonNull(include) && include.contains(Message.KEY_MANY))
      out.put(LinkMessage.KEY_MANY, JSON.arrayOf(linkMessageDAO.readAllInLinks(access, linkIds)));

    if (Objects.nonNull(include) && include.contains(MemeEntity.KEY_MANY))
      out.put(LinkMeme.KEY_MANY, JSON.arrayOf(linkMemeDAO.readAllInLinks(access, linkIds)));

    if (Objects.nonNull(include) && include.contains(ChordEntity.KEY_MANY))
      out.put(LinkChord.KEY_MANY, JSON.arrayOf(linkChordDAO.readAllInLinks(access, linkIds)));

    if (Objects.nonNull(include) && include.contains(Choice.KEY_MANY))
      out.put(Choice.KEY_MANY, JSON.arrayOf(choiceDAO.readAllInLinks(access, linkIds)));

    return out;
  }

  /**
   Read all links, optionally from offset or seconds UTC

   @param access control
   @return links
   @throws Exception on failure
   */
  private Result<LinkRecord> readAllLinks(Access access) throws Exception {

    if (Objects.nonNull(fromOffset))
      return linkDAO.readAllFromOffset(access, ULong.valueOf(chainId), ULong.valueOf(fromOffset));

    if (Objects.nonNull(fromSecondsUTC))
      return linkDAO.readAllFromSecondsUTC(access, ULong.valueOf(chainId), ULong.valueOf(fromSecondsUTC));

    return linkDAO.readAll(access, ULong.valueOf(chainId));
  }

  /**
   Get an immutable list of ids from a result of Links

   @param links to get ids of
   @return list of ids
   */
  private ImmutableList<ULong> linkIds(Result<LinkRecord> links) {
    ImmutableList.Builder<ULong> builder = ImmutableList.builder();
    links.forEach(linkRecord -> builder.add(linkRecord.getId()));
    return builder.build();
  }

}
