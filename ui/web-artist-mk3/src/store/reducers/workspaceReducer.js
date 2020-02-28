/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// app
import {produce} from "immer";
import {toEntityName} from "../../util";

// Constants to identify requests handled by the api ORM Reducer
export const
  WORKSPACE_UPDATE = "WORKSPACE_UPDATE",
  WORKSPACE_ACTIVATE = "WORKSPACE_ACTIVATE",
  WORKSPACE_DEACTIVATE = "WORKSPACE_DEACTIVATE";

// Key in which to store the child type of a parent Id
export const activationKey = (parentId, childType) => `${parentId}_${toEntityName(childType)}`;

// Setup the initial state
const initialState = {
  program: {},

  // timeline grid and initial value
  timelineGrid: 0.125,

  // zoom level and initial value
  zoomLevel: 1.0,

  // UI has any number of keys for things that are active
  active: {},

  // geometry
  trackHeight: 120,
  voicePanelWidth: 220,
  trackPanelWidth: 120,
  beatWidth: 100,
};

/**
 * The ORM reducer that processes a request to update a key/value pair in the workspace
 * @param baseState to update
 * @param {{
 *     type:string,
 *     payload:{
 *         key:string,
 *         value:Object,
 *     }
 * }} action to reduce
 * @return {*} updated state
 */
const workspaceReducer = function (baseState, action) {

  // previous state, else (if undefined) new initial state
  const state = !!baseState ? baseState : initialState;

  // use mutate the requested key+value
  switch (action.type) {
    case WORKSPACE_UPDATE:
      return produce(baseState, draftState => {
        draftState[action.payload.key] = action.payload.value
      });

    case WORKSPACE_ACTIVATE:
      return produce(baseState, draftState => {
        draftState.active[action.payload.key] = action.payload.value
      });

    case WORKSPACE_DEACTIVATE:
      return produce(baseState, draftState => {
        delete draftState.active[action.payload.key];
      });

    default:
      return state;
  }
};

// public
export default workspaceReducer;
