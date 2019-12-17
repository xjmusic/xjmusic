// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// vendor
import React, {useState} from 'react';
import Select from 'react-select';
import {useSelector} from "react-redux";
// app
import "./EntityChildDropdownSelector.scss"
import {selectStyles, selectTheme} from "./SelectorStyles";
import ActionButton from "../action/ActionButton";
import SearchIcon from "../icon/SearchIcon";
import DropdownIcon from "../icon/DropdownIcon";

/**
 EntityChildDropdownSelector for editing one attribute of an entity

 @return {*}
 @typedef EntitiesSelector{Selector} is a Redux selector that can be used to fetch the entities for this selector
 @typedef Entity{{ id:String }} is an entity having an id
 @typedef TextFormattingFunction{function} given an entity, returns it formatted as text
 @typedef ClassName{string} of attribute to edit
 @typedef ActivateFunction{function} to callback when a selection is made
 @param {{
   entitiesSelector EntitiesSelector,
   format TextFormattingFunction,
   className ClassName,
   active Entity,
   onActivate ActivateFunction,
 }} props describing the child selector to build
 */
export default function EntityChildDropdownSelector(props) {
  const
    [isOpen, setIsOpen] = useState(false);

  // option object has id and text, must be translated back and forth value <> riek field
  const entities = useSelector(state => props.entitiesSelector(state)),
    options = entities
      .map((o) => ({value: o.id, label: props.format(o)})),
    active = !!props.active ? options.find((o) => (o.value === props.active.id)) : null;


  const
    toggleOpen = () => {
      setIsOpen(!isOpen);
    },
    onSelectChange = option => {
      toggleOpen();
      props.onActivate(option.value);
    };

  return (
    <Dropdown
      isOpen={isOpen}
      onClose={toggleOpen}
      target={
        <ActionButton className="dropdown-toggle" onClick={toggleOpen}>
          <DropdownIcon/>
        </ActionButton>
      }>
      <Select
        autoFocus
        classNamePrefix="selector"
        options={options}
        value={active}
        backspaceRemovesValue={false}
        components={{DropdownIndicator: SearchIcon, IndicatorSeparator: null}}
        controlShouldRenderValue={false}
        hideSelectedOptions={false}
        isClearable={false}
        menuIsOpen
        onChange={onSelectChange}
        placeholder="Search..."
        styles={selectStyles}
        theme={selectTheme}
        tabSelectsValue={false}/>
    </Dropdown>
  );

}

const Menu = props => {
  return (
    <div className="dropdown-menu"
         {...props}
    />
  );
};

const Blanket = props => (
  <div className="dropdown-blanket"
       {...props}
  />
);

const Dropdown = ({children, isOpen, target, onClose}) => (
  <div className={`entity-child-dropdown-selector ${isOpen ? "active" : ""}`}>
    {target}
    {isOpen ? <Menu>{children}</Menu> : null}
    {isOpen ? <Blanket onClick={onClose}/> : null}
  </div>
);
