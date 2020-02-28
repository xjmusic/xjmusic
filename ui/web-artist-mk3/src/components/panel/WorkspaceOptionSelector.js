/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// vendor
import React from "react";
import {useDispatch} from "react-redux";
// app
import "./WorkspaceOptionSelector.scss"
import workspaceUpdate from "../../store/actions/workspaceUpdate";
import {optional, visibleIf} from "../../util";


/**
 WorkspaceOptionSelector for selecting from a set of options to set a workspace value

 @return {*}
 @typedef WorkspaceOptionName{string} is the key in the workspace state slice
 @typedef OptionValue{string} value of one option
 @typedef Label{string} of attribute to display next to field
 @typedef ClassName{string} to be added to wrapper element class
 @typedef IsDisabled{Boolean} whether this element should be disabled
 @typedef OptionDefinition{ label:String, value:OptionValue, disabled:IsDisabled } for each option in the menu
 @param {{
   className ClassName,
   name WorkspaceOptionName,
   label Label (optional),
   value OptionValue,
   options [OptionDefinition],
 }} props describing the workspace option to set
 */
export default function WorkspaceOptionSelector(props) {
  const
    label = optional(props.label),
    name = props.name,
    options = props.options,
    activeValue = props.value,
    dispatch = useDispatch();

  const
    selectOption = (value) =>
      dispatch(workspaceUpdate(name, value));

  return (
    <div className={`workspace-option-selector ${props.className}`}>
      {visibleIf(label, <label>{props.label}</label>)}
      <select onChange={(event) => {
        selectOption(event.target.value)
      }} value={activeValue}>
        {options.map((option) => (
          <option key={`${name}-${option.value}`}
                  value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </div>
  );
};

