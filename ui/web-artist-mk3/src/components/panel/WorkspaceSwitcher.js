/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// vendor
import React from "react";
import {useDispatch} from "react-redux";
// app
import "./WorkspaceSwitcher.scss"
import workspaceActivate from "../../store/actions/workspaceActivate";
import ActionButton from "../action/ActionButton";


/**
 ActionButton for editing one attribute of an entity

 @return {*}
 @typedef EntityId{string} is a UUID
 @typedef OptionValue{string} value of one option
 @typedef ClassName{string} to be added to wrapper element class
 @typedef IsDisabled{Boolean} whether this element should be disabled
 @typedef OptionDefinition{ label:String, value:OptionValue, disabled:IsDisabled } for each option in the menu
 @param {{
   className ClassName,
   parentId EntityId
   type SwitchType
   value OptionValue
   options [OptionDefinition]
 }} props describing the child selector to build
 */
export default function WorkspaceSwitcher(props) {
  const
    parentId = props.parentId,
    childType = props.type,
    options = props.options,
    activeValue = props.value,

    //
    dispatch = useDispatch(),

    //
    switchTo = (value) =>
      dispatch(workspaceActivate(parentId, childType, value));

  return (
    <div className={`workspace-switcher ${props.className}`}>
      {options.map((option) => (
        <ActionButton key={`workspace-switch-${option.value}`} disabled={!!option.disabled}
                      className={activeValue === option.value ? "active" : "ready"}
                      onClick={() => switchTo(option.value)}>
          {option.label}
        </ActionButton>
      ))}

    </div>
  );
};

