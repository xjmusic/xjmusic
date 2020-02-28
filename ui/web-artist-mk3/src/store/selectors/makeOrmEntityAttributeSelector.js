/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// vendor
import {createSelector} from "reselect";
// app
import orm from "../orm";
import {toEntityName} from "../../util";

const VALUE_IF_NOT_PRESENT = "";

/**
 Select the value of an attribute of an ORM model

 @param name of ORM model
 @param id of entity or null if field is inactive
 @param attrName to select from entity
 @returns {*}
 */
export default (name, id, attrName) => {
  const entityName = toEntityName(name);
  return createSelector(
    [(state) => {
      const entity = orm.session(state.orm)[entityName].withId(id);

      return (
        entity &&
        '_fields' in entity &&
        attrName in entity._fields &&
        entity._fields.hasOwnProperty(attrName)
      )
        ? entity._fields[attrName]
        : VALUE_IF_NOT_PRESENT;
    }],
    (value) => {
      return value;
    })
}
