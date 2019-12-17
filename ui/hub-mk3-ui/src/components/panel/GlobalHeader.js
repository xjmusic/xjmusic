// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// vendor
import React from "react";
import {Link} from "react-router-dom";
import {RSAA} from "redux-api-middleware";
import {useDispatch, useSelector} from "react-redux";
// app
import "./GlobalHeader.scss"
import {API_AUTH_FAILURE, API_AUTH_REQUEST, API_AUTH_SUCCESS} from "../../store/reducers/apiAuthReducer"
import {API_ORM_FAILURE, API_ORM_REQUEST, API_ORM_SUCCESS} from "../../store/reducers/apiOrmReducer"
import {selectUser} from "../../store/selectors/authSelectors";
import UserAvatar from "../icon/UserAvatar";

/**
 ProgramEditor component
 @return {*}
 @constructor
 */
export default function ProgramEditor(props) {
  const dispatch = useDispatch();

  const user = useSelector(state => selectUser(state));

  dispatch(
    {
      [RSAA]: {
        endpoint: `/api/1/auth`,
        method: 'GET',
        types: [API_AUTH_REQUEST, API_AUTH_SUCCESS, API_AUTH_FAILURE]
      }
    });

  dispatch(
    {
      [RSAA]: {
        endpoint: `/api/1/users/me`,
        method: 'GET',
        types: [API_ORM_REQUEST, API_ORM_SUCCESS, API_ORM_FAILURE]
      }
    });

  return (
    <header className="global-header">
      <Link className="logo" to="/">&nbsp;</Link>
      <div className="title">&nbsp;</div>
      <UserAvatar user={user}/>
    </header>
  );
};
