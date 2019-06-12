// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.segment;

import io.xj.core.dao.SegmentDAO;
import io.xj.core.model.user.role.UserRoleType;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 Segment record
 */
@Path("segments/{id}")
public class SegmentOneResource extends HubResource {

  @PathParam("id")
  String id;

  /**
   Get one segment.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc) {
    return readOne(crc, dao(), id);
  }

  /**
   Get DAO from injector

   @return DAO
   */
  private SegmentDAO dao() {
    return injector.getInstance(SegmentDAO.class);
  }
}
