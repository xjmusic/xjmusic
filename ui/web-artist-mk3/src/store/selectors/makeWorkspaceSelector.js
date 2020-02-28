/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import {createSelector} from "reselect";

/**
 * Selects workspace value by key
 */
export default (key) => {
  return createSelector(
    [(state) => {
      return state.workspace[key];
    }],
    (value) => {
      return value;
    });
};
