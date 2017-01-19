package io.outright.xj.core.app.server;

import io.outright.xj.core.app.config.Config;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;

public class HttpResponseProviderImpl implements HttpResponseProvider {
  private final String appUrl = Config.appBaseUrl();

  @Override
  public Response internalRedirect(String path) {
    return Response.temporaryRedirect(URI.create(appUrl+path)).build();
  }

  @Override
  public Response internalRedirectWithCookie(String path, NewCookie... cookies) {
    return Response
      .temporaryRedirect(URI.create(appUrl+path))
      .cookie(cookies)
      .build();
  }

  @Override
  public Response unauthorizedWithCookie(NewCookie... cookies) {
    return Response
      .status(Response.Status.UNAUTHORIZED)
      .cookie(cookies)
      .build();
  }

  @Override
  public Response unauthorized() {
    return Response
      .status(Response.Status.UNAUTHORIZED)
      .build();
  }

}
