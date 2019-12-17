// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// vendor
import _ from 'lodash'
import React from "react";
import {RIEInput, RIENumber, RIESelect} from "riek";
import {useDispatch} from "react-redux";
// app
import "./EntityAttributeField.scss"
import {update} from "../../store/actions/apiEntities";
import {optional} from "../../util";

/**
 EntityAttributeField for editing one attribute of an entity

 @return {*}
 @typedef EntityType{string} e.g. "libraries" or "program-sequences"
 @typedef EntityId{string} is a UUID or null if field is inactive
 @typedef AttributeName{string} of attribute to edit
 @typedef Label{string} of attribute to display next to field
 @typedef AttributeType{string} of edit-in-place field, one of "text" "number" or "select"
 @typedef AttributeOptions{string} for a select-type edit-in-place field only, array of strings
 @param {{
   type EntityType,
   entity object (optional) and if empty no field will be shown,
   attrName AttributeName,
   label Label (optional),
   className String (optional),
   attrType AttributeType,
   attrOptions AttributeOptions,
 }} props describing an entity and which attributes to edit
 */
export default function EntityAttributeField(props) {
  const
    dispatch = useDispatch(),
    entity = optional(props.entity),
    value = !!entity && props.attrName in entity ? entity[props.attrName] : null;

  // when a field value changes, only update if attribute value changed
  const didChange = (attributes) => {
    if (attributes[props.attrName] !== value)
      dispatch(update(props.type, entity.id, attributes));
  };

  // option object has id and text, must be translated back and forth value <> riek field
  const
    toOpt = (o) => {
      return {id: o, text: o};
    },
    ofOpt = (o) => {
      return o.text;
    };

  // different template for each attribute type
  if (!entity)
    return <div>&nbsp;</div>;

  const elementClass = `entity-attribute field ${props.attrName} ${optional(props.className, "")}`;
  switch (props.attrType) {

    case "text":
      return (
        <div className={elementClass}>
          <label>{props.label}</label>
          <RIEInput
            value={value}
            propName={props.attrName}
            validate={_.isString}
            change={didChange}/>
        </div>
      );

    case "number":
      return (
        <div className={elementClass}>
          <label>{props.label}</label>
          <RIENumber
            value={value}
            propName={props.attrName}
            validate={(x) => x >= 0}
            change={didChange}/>
        </div>
      );

    case "select":
      return (
        <div className={elementClass}>
          <label>{props.label}</label>
          <RIESelect
            value={toOpt(value)}
            propName={props.attrName}
            options={props.attrOptions.map((o) => toOpt(o))}
            change={(attributes) => {
              didChange({
                [props.attrName]: ofOpt(attributes[props.attrName]),
              })
            }}/>
        </div>
      );

    default:
      // noop
      break;
  }

};

