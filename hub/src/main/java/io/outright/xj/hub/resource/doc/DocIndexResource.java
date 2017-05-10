// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.hub.resource.doc;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.internal.DocProvider;
import io.outright.xj.core.model.doc.Doc;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 Documentation, with access control
 */
@Path("docs")
public class DocIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);
  private final DocProvider docProvider = injector.getInstance(DocProvider.class);

  /**
   Read index of docs
   [#215] Internal "Docs" section where users of different permissions can view static content that is stored in .md static files on the backend, for easy editing.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST, Role.ENGINEER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    try {
      return Response
        .accepted(JSON.wrap(Doc.KEY_MANY, docProvider.keysToJSONArray(docProvider.fetchIndex())).toString())
        .type(MediaType.APPLICATION_JSON)
        .build();

    } catch (Exception e) {
      return response.notFound(Doc.KEY_MANY);
    }
  }

}
