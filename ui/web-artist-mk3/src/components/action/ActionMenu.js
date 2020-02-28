/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// vendor
import React, {useState} from "react";
import Popup from "reactjs-popup";
// app
import "./ActionMenu.scss"
import MenuIcon from "../icon/MenuIcon";
import {optional} from "../../util";

// Default popup position
const DEFAULT_POPUP_POSITION = "right center";

/**
 ActionMenu for taking an actions on an entity

 @return {*}
 @typedef ClassName{string} to be added to wrapper element class
 @typedef Label{string} of attribute to display next to field
 @typedef PopupPosition{string} (optional) position of popup, falls back to defaults
 @typedef ActionFunction{function} to callback when button is clicked
 @typedef IsDisabled{Boolean} whether this element should be disabled
 @typedef ActionDefinition{ label:String, action:ActionFunction, disabled:IsDisabled } for each option in the menu
 @param {{
   className ClassName,
   position PopupPosition,
   disabled IsDisabled,
   actions [ActionDefinition],
 }} props describing an entity and which attributes to edit
 */
export default function ActionMenu(props) {
  const [isOpen, setIsOpen] = useState(false),
    position = optional(props.position, DEFAULT_POPUP_POSITION);

  let classNames = [];
  if (props.className) classNames.push(props.className);
  if (props.disabled) classNames.push("disabled");
  if (!!isOpen) classNames.push("open");

  return (
    <div className={`action-menu ${classNames.join(" ")}`}>
      <Popup open={isOpen}
             disabled={props.disabled}
             trigger={() => (<div className="open-menu"><MenuIcon/></div>)}
             onOpen={() => setIsOpen(true)}
             onClose={() => setIsOpen(false)}
             position={position}>
        <div className="actions">{props.actions.map((action) => (
          <button key={action.label} onClick={function () {
            setIsOpen(false);
            action.action();
          }} disabled={!!action.disabled}>{action.label}</button>
        ))}</div>
      </Popup>
    </div>
  );
};

