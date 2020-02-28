/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// vendor
import {toastr} from "react-redux-toastr";
import {API_ORM_CLONED, API_ORM_DELETED, API_ORM_FAILURE} from "../reducers/apiOrmReducer";
import {API_AUTH_FAILURE} from "../reducers/apiAuthReducer";
import {toEntityName} from "../../util";

/**
 * Text for known status codes
 * @param code code to use known text for, if found
 * @param defaultText to fallback on
 * @returns {*} text if known, else default text
 */
const textIfKnown = (code, defaultText) => {
    return !!code && code in knownStatusText ?
      knownStatusText[code] :
      defaultText
  },
  knownStatusText = {
    404: "Not Found",
    "CoreException": "Oops!",
  };

/**
 * Dispatches a Toastr message when it encounters an api FAILURE type action
 * @param store for middleware
 * @returns {function(*): function(*=): *} middleware
 */
export default store => next => action => {
  const
    status =
      action &&
      "payload" in action &&
      "object" === typeof action.payload &&
      "status" in action.payload ?
        action.payload.status :
        null,
    description = action.description ? action.description : (
      action.entity ? toEntityName(action.entity) : "n/a"
    );

  switch (action.type) {

    case API_ORM_DELETED:
      toastr.success("It was done", `Deleted ${description}`, {timeOut: 1500});
      break;

    case API_ORM_CLONED:
      toastr.success("It was done", `Cloned ${description}`, {timeOut: 1500});
      break;

    case API_ORM_FAILURE:
      if (action &&
        "payload" in action &&
        "response" in action.payload &&
        action.payload.response &&
        "errors" in action.payload.response &&
        action.payload.response.errors.length) {
        action.payload.response.errors.forEach((error) => {
          toastr.error(
            textIfKnown(error.code, "API Failure"),
            "title" in error ? error.title : "No Details"
          );
        });
      } else {
        toastr.error(`API Failure`, `Code ${status}`);
      }
      break;

    case API_AUTH_FAILURE:
      toastr.error(`Authentication Failure`, `Code ${status}`);
      break;

    default:
      break;
  }

  return next(action);
};
