/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// vendor
import React from "react";
// app
import "./ActionButton.scss"
import {optional} from "../../util";

/**
 EntityAttributeEditor for taking an action on an entity

 @return {*}
 @typedef ClassName{string} to be added to wrapper element class
 @typedef Label{string} of attribute to display next to field
 @typedef ActionFunction{function} to callback when button is clicked
 @typedef IsDisabled{Boolean} whether this element should be disabled
 @param {{
   className ClassName,
   label Label,
   onClick ActionFunction,
   disabled IsDisabled,
 }} props describing an entity and which attributes to edit
 */
export default function ActionButton(props) {
  return (
    <div className={`action-button ${optional(props.className, "")}`}>
      <button {...props}/>
    </div>
  );
};

