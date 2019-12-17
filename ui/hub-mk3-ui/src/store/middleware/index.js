// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// vendor
import {getDefaultMiddleware} from "@reduxjs/toolkit";
import {apiMiddleware} from "redux-api-middleware";
// app
import apiResponseMiddleware from "./apiResponseMiddleware";

/**
 * Configure all middleware for the Redux store
 */
export default [
  ...getDefaultMiddleware(),
  apiMiddleware,
  apiResponseMiddleware
]
