/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// vendor
import {createSelector} from "reselect";
import orm from "../orm";
import {toBelongsKey, toEntityName} from "../../util";
// app

/**
 Select the set of child entities of a given parent, via ORM

 @param type of ORM model to select, e.g. "program" or "program voice"
 @param relType of parent entity, e.g. "program" or "program voice"
 @param relId of parent entity, a UUID
 @param alsoRelType (optional) second type of parent entity
 @param alsoRelId (optional) second id of parent entity
 @returns {*} array of redux ORM models
 */
export default (type, relType, relId, alsoRelType = null, alsoRelId = null) => {
  const entityName = toEntityName(type);
  const relKey = `${toBelongsKey(relType)}Id`;
  const alsoRelKey = !!alsoRelType ? `${toBelongsKey(alsoRelType)}Id` : null;
  return createSelector(
    [(state) => {
      if (!relId) return [];
      const sess = orm.session(state.orm);
      if (!(entityName in sess)) return [];
      return sess[entityName].all()
        .filter((entity => {
            if (!!alsoRelKey)
              return entity[alsoRelKey] === alsoRelId && entity[relKey] === relId;
            else
              return entity[relKey] === relId
          }
        ))
        .orderBy("name")
        .toModelArray();
    }],
    (models) => {
      return models;
    });
};
