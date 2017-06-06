// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.hub.resource.doc;

import io.xj.core.CoreModule;
import io.xj.core.app.server.HttpResponseProvider;
import io.xj.core.internal.DocProvider;
import io.xj.core.model.doc.Doc;
import io.xj.core.model.role.Role;
import io.xj.core.transport.JSON;
import io.xj.core.util.Text;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 Documentation, with access control
 */
@Path("docs/{id}")
public class DocRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);
  private final DocProvider docProvider = injector.getInstance(DocProvider.class);

  @PathParam("id")
  String id;

  /**
   Read one doc
   [#215] Internal "Docs" section where users of different permissions can view static content that is stored in .md static files on the backend, for easy editing.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST, Role.ENGINEER})
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    String key = Text.DocKey(id);

    try {
      Doc doc = docProvider.fetchOne(key);
      return Response
        .accepted(JSON.wrap(Doc.KEY_ONE, doc.toJSONObject()).toString())
        .type(MediaType.APPLICATION_JSON)
        .build();


    } catch (Exception e) {
      return response.notFound(key);
    }
  }

}
