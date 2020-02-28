/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// vendor
import React from "react";
// app
import "./ProgramSequenceChord.scss"
import EntityAttributeField from "../entity/EntityAttributeField";
import DestroyIcon from "../icon/DestroyIcon";
import {destroy} from "../../store/actions/apiEntities";
import {isSuccessful} from "../../util";
import workspaceDeactivate from "../../store/actions/workspaceDeactivate";
import {useDispatch} from "react-redux";

/**
 ProgramSequenceChord for showing one chord in a program sequence

 @return {*}
 @typedef Chord{{
              id:string,
              name string,
              position: float,
          }}
 @param {{
   chord Chord,
   pixelsPerBeat Number,
 }} props describing an entity and which attributes to edit
 */
export default function ProgramSequenceChord(props) {
  const
    chord = props.chord,
    dispatch = useDispatch();

  const
    destroyChord = () => {
      dispatch(destroy(
        "program sequence chord", `Chord`, chord.id
      )).then(
        (response) => {
          if (isSuccessful(response))
            dispatch(workspaceDeactivate(chord.programSequenceId, "program sequence chord"))
        })
    };

  return (
    <div className="program-sequence-chord"
         style={{
           left: `${Math.floor(props.pixelsPerBeat * chord.position)}px`,
           width: `${Math.floor(props.pixelsPerBeat)}px`,
         }}>

      <EntityAttributeField
        className="position"
        attrType="number"
        attrName="position"
        type="program sequence chord"
        entity={chord}/>

      <EntityAttributeField
        className="name"
        attrType="text"
        attrName="name"
        type="program sequence chord"
        entity={chord}/>

      <div className="destroy" onClick={destroyChord}><DestroyIcon scale={0.6}/></div>
    </div>
  );
};

