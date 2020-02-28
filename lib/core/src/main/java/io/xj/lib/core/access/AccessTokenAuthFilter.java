// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.access;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;

public interface AccessTokenAuthFilter extends ContainerRequestFilter {

  void setResourceInfo(ResourceInfo resourceInfo);
}
