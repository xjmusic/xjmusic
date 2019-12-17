// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// vendor
import React, {useState} from "react";
import Popup from "reactjs-popup";
// app
import "./ActionMenu.scss"
import {optional, visibleIf} from "../../util";

/**
 ActionMenu for taking an actions on an entity

 @return {*}
 @typedef ClassName{string} to be added to wrapper element class
 @typedef Label{string} of attribute to display next to field
 @typedef ActionFunction{function} to callback when button is clicked
 @typedef ActionDefinition{label:String,action:ActionFunction,disabled:Boolean} for each option in the menu
 @typedef IsDisabled{Boolean} whether this element should be disabled
 @param {{
   className ClassName,
   actions [ActionDefinition],
 }} props describing an entity and which attributes to edit
 */
export default function EntityActionMenu(props) {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div className={`" ${optional(props.className, "")}`}>
      <div className={`open-menu ${visibleIf(isOpen, "open")}`} onClick={() => setIsOpen(true)}>&nbsp;</div>
      <Popup open={isOpen}
             position="right center">
        <div>{props.actions.map((action) => (
          <button key={action.label} onClick={() => {
            setIsOpen(false);
            action.action();
          }} disabled={!!action.disabled}>{action.label}</button>
        ))}</div>
      </Popup>
    </div>
  );
};

