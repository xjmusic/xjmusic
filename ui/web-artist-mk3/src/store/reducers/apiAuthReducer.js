/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// Constants to identify requests handled by the api ORM Reducer
export const API_AUTH_REQUEST = "API_AUTH_REQUEST";
export const API_AUTH_SUCCESS = "API_AUTH_SUCCESS";
export const API_AUTH_FAILURE = "API_AUTH_FAILURE";

// Setup the initial state
const initialState = {
  userAuthId: "",
  roles: "",
  accounts: "",
  userId: "",
};

/**
 * The ORM reducer that processes an API /auth response, deserializes the data, and updates state
 * @param baseState to update
 * @param action to call next
 * @return {*} updated state
 */
const apiAuthReducer = function (baseState, action) {

  // previous state, else (if undefined) new initial state
  const state = !!baseState ? baseState : initialState;

  // If it's not a successful API auth request, skip
  if (action.type !== API_AUTH_SUCCESS) return state;

  return action.payload;
};

// public
export default apiAuthReducer;
