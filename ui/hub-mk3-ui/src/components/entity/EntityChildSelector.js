// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// vendor
import React from "react";
import {useSelector} from "react-redux";
import Select from "react-select";
// app
import {selectStyles, selectTheme} from "./SelectorStyles";

/**
 EntityChildSelector for editing one attribute of an entity

 @return {*}
 @typedef EntitiesSelector{Selector} is a Redux selector that can be used to fetch the entities for this selector
 @typedef EntityId{string} is a UUID
 @typedef TextAttribute{string} name of attribute to use as text value for option
 @typedef ClassName{string} of attribute to edit
 @typedef Label{string} of attribute to display next to field
 @typedef ActivateFunction{function} to callback when a selection is made
 @param {{
   entitiesSelector EntitiesSelector,
   label Label,
   textAttr TextAttribute,
   className ClassName,
   activeId EntityId,
   onActivate ActivateFunction,
   autoFocus Boolean (optional),
   menuIsOpen Boolean (optional),
 }} props describing the child selector to build
 */
export default function EntityChildSelector(props) {
  const entities = useSelector(state => props.entitiesSelector(state));

  // option object has id and text, must be translated back and forth value <> riek field
  const
    options = entities
      .map((o) => ({value: o.id, label: o[props.textAttr]})),
    active = !!props.activeId ? options.find((o) => (o.value === props.activeId)) : null;

  return (
    <div className={`entity-attribute field ${props.className}`}>
      <label>{props.label}</label>

      <Select
        classNamePrefix="selector"
        autoFocus={!!props.autoFocus}
        menuIsOpen={!!props.menuIsOpen}
        options={options}
        value={active}
        styles={selectStyles}
        theme={selectTheme}
        onChange={(option) => (props.onActivate(option.value))}/>

    </div>
  );
};

