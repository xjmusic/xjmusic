/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// vendor
import {camelize} from "inflected"

/**
 Deserialize JSON:API data
 into a standard entity,
 with parentId for belongs-to relationships

 @param data to deserialize
 @return POJO entity deserialized from data
 */
export default function deserialize(data) {
  let obj = {};

  // add id to obj
  if ("id" in data) obj.id = data.id;

  // add all attributes to obj
  if ("attributes" in data)
    for (let key in data.attributes) if (data.attributes.hasOwnProperty(key))
      obj[camelize(key, false)] = data.attributes[key];

  // for all relationships, if data is NOT an array, it's a belongs-to and we should add relationshipId to obj
  if ("relationships" in data)
    for (let key in data.relationships) if (data.relationships.hasOwnProperty(key))
      if ("data" in data.relationships[key] && !Array.isArray(data.relationships[key].data))
        obj[`${key}Id`] = data.relationships[key].data.id;

  return obj;
}
