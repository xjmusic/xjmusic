// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.tables.pojos.Library;

import java.util.UUID;

public interface LibraryManager extends Manager<Library> {

  /**
   * Provide an entity containing some new properties, but otherwise clone everything of a source library, of new record, and return it.
   * When a library is cloned, also clone all programs/instruments within it https://www.pivotaltracker.com/story/show/181196881
   *
   * @param access  control
   * @param cloneId of library to clone
   * @param entity  for the new Library
   * @return newly readMany record
   */
  ManagerCloner<Library> clone(HubAccess access, UUID cloneId, Library entity) throws ManagerException;

}
