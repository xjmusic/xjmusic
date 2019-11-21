// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.access;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;

public interface AccessTokenAuthFilter extends ContainerRequestFilter {

  /**
   Override resource info-- FOR TESTING PURPOSES ONLY, in order to mock a resource

   @param resourceInfo to set
   */
  void setTestResources(ResourceInfo resourceInfo, AccessControlProvider accessControlProvider);

}
