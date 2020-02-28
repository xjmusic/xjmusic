/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// vendor
import {createSelector} from "reselect";
// app
import orm from "../orm";
import {toEntityName} from "../../util";

/**
 Select an ORM model

 @param name of ORM model
 @param id of entity
 @returns {*}
 */
export default (name, id) => {
  const entityName = toEntityName(name);
  return createSelector(
    [(state) => {
      if (!id) return null;
      return orm.session(state.orm)[entityName].withId(id);
    }],
    (entity) => {
      return entity;
    })
}
