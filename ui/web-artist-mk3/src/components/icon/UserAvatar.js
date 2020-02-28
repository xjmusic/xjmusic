/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// vendor
import React from "react";
// app
import "./UserAvatar.scss"
import {Link} from "react-router-dom";

/**
 UserAvatar component
 @return {*}
 @constructor
 */
export default function UserAvatar(props) {
  const user = props.user;

  if (user)
    return (
      <Link className="avatar" to={`/users/${user.id}`}>
        <img alt={user.name} src={user.avatarUrl}/>
      </Link>
    );
  else
    return (
      <div className="avatar">
        <img alt="" src="/assets/images/avatar/anonymous.jpg"/>
      </div>
    );

};
