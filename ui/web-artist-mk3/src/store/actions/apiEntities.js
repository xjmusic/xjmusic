/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import {RSAA} from "redux-api-middleware";
import {API_ORM_CLONED, API_ORM_DELETED, API_ORM_FAILURE, API_ORM_REQUEST, API_ORM_SUCCESS} from "../reducers/apiOrmReducer";
import {isSuccessful, toEntityType} from "../../util";

const
  HEADER_ACCEPT_JSON_API = {'Accept': 'application/vnd.api+json', 'Content-Type': 'application/vnd.api+json'},
  STANDARD_API_RESPONSE_TYPES = [API_ORM_REQUEST, API_ORM_SUCCESS, API_ORM_FAILURE];


/**
 Action to create any entity with attributes

 @param {String} entity type to create
 @param {Object} attributes of entity to create
 @param {Object} relationships of entity to create
 @returns {Object} action to dispatch
 */
export const create = (entity, attributes, relationships) => {
  const type = toEntityType(entity);
  return {
    [RSAA]: {
      body: JSON.stringify({
        data: {
          type,
          attributes,
          relationships,
        }
      }),
      headers: HEADER_ACCEPT_JSON_API,
      endpoint: `/api/1/${type}/`,
      method: 'POST',
      types: STANDARD_API_RESPONSE_TYPES,
    },
  }
};

/**
 Action to get any entity

 @param {String} entity type to read
 @param {String} id of entity
 @param {String} included (optional) CSV of included entities
 @returns {Object} action to dispatch
 */
export const read = (entity, id, included) => {
  const type = toEntityType(entity);
  return {
    [RSAA]: {
      endpoint: included ? `/api/1/${type}/${id}?include=${included}` : `/api/1/${type}/${id}`,
      method: 'GET',
      types: STANDARD_API_RESPONSE_TYPES
    }
  }
};

/**
 Action to patch attributes of any entity

 @param {String} entity type to update
 @param {String} id of entity
 @param {Object} attributes to update
 @returns {Object} action to dispatch
 */
export const update = (entity, id, attributes) => {
  const type = toEntityType(entity);
  return {
    [RSAA]: {
      body: JSON.stringify({
        data: {
          type,
          id,
          attributes,
        }
      }),
      headers: HEADER_ACCEPT_JSON_API,
      endpoint: `/api/1/${type}/${id}`,
      method: 'PATCH',
      types: STANDARD_API_RESPONSE_TYPES
    }
  }
};

/**
 Action to clone any entity

 @param {String} entity type to clone
 @param {String} description to display in confirmation notification
 @param {String} id of entity
 @param {Object} attributes (optional) of entity to create
 @param {Object} relationships (optional) of entity to create
 @returns {Object} action to dispatch
 */
export const clone = (entity, description, id, attributes={}, relationships={}) => {
  const type = toEntityType(entity);
  return function (dispatch) {
    return dispatch({
      [RSAA]: {
        body: JSON.stringify({
          data: {
            type,
            attributes,
            relationships,
          }
        }),
        headers: HEADER_ACCEPT_JSON_API,
        endpoint: `/api/1/${type}?cloneId=${id}`,
        method: 'POST',
        types: STANDARD_API_RESPONSE_TYPES,
      }
    }).then(
      (response) => {
        if (isSuccessful(response))
          dispatch({
            type: API_ORM_CLONED,
            entity,
            description,
            id,
          });
        return response;
      }
    )
  }
};

/**
 Action to destroy any entity

 @param {String} entity type to destroy
 @param {String} description to display in confirmation notification
 @param {String} id of entity
 @returns {Object} action to dispatch
 */
export const destroy = (entity, description, id) => {
  const type = toEntityType(entity);
  return function (dispatch) {
    return dispatch({
      [RSAA]: {
        headers: HEADER_ACCEPT_JSON_API,
        endpoint: `/api/1/${type}/${id}`,
        method: 'DELETE',
        types: STANDARD_API_RESPONSE_TYPES
      }
    }).then(
      (response) => {
        if (isSuccessful(response))
          dispatch({
            type: API_ORM_DELETED,
            entity,
            description,
            id,
          });
        return response;
      }
    )
  }
};
