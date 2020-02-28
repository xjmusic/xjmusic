/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import {createSelector} from "reselect";
import orm from "../orm";
import {toEntityName} from "../../util";

/**
 Selects whether a program of a given id exists in the store

 @param name to check for presence in the store
 @param entityId to check for presence in the store
 */
export default (name, entityId) => {
  const entityName = toEntityName(name);
  return createSelector(
    [(state) => {
      return orm.session(state.orm)[entityName].idExists(entityId);
    }],
    (isProgramInStore) => {
      return isProgramInStore;
    });
};
