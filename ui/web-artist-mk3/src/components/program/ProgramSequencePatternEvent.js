/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// vendor
import React from "react";
// app
import "./ProgramSequencePatternEvent.scss"
import EntityAttributeField from "../entity/EntityAttributeField";
import DestroyIcon from "../icon/DestroyIcon";
import {destroy} from "../../store/actions/apiEntities";
import {isSuccessful} from "../../util";
import workspaceDeactivate from "../../store/actions/workspaceDeactivate";
import {useDispatch} from "react-redux";

/**
 ProgramSequencePatternEvent for showing one pattern event in a program sequence

 @return {*}
 @typedef Event{{
              id:string,
              name string,
              position: float,
          }}
 @param {{
   event Event,
   pixelsPerBeat Number,
 }} props describing an entity and which attributes to edit
 */
export default function ProgramSequencePatternEvent(props) {
  const
    event = props.event,
    dispatch = useDispatch();

  const
    destroyPatternEvent = () => {
      dispatch(destroy(
        "program sequence pattern event", `Event`, event.id
      )).then(
        (response) => {
          if (isSuccessful(response))
            dispatch(workspaceDeactivate(event.programSequenceId, "program sequence pattern event"))
        })
    };

  return (
    <div className="program-sequence-pattern-event"
         style={{
           left: `${Math.floor(props.pixelsPerBeat * event.position)}px`,
           width: `${Math.floor(props.pixelsPerBeat * event.duration)}px`,
         }}>

      <EntityAttributeField
        className="position"
        attrType="number"
        attrName="position"
        type="program sequence pattern event"
        entity={event}/>

      <EntityAttributeField
        className="duration"
        attrType="number"
        attrName="duration"
        type="program sequence pattern event"
        entity={event}/>

      <EntityAttributeField
        className="note"
        attrType="text"
        attrName="note"
        type="program sequence pattern event"
        entity={event}/>

      <EntityAttributeField
        className="velocity"
        attrType="number"
        attrName="velocity"
        type="program sequence pattern event"
        entity={event}/>

      <div className="destroy" onClick={destroyPatternEvent}><DestroyIcon scale={0.6}/></div>
    </div>
  );
};

