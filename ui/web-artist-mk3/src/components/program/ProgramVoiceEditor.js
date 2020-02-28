/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// vendor
import React from "react";
import {useDispatch, useSelector} from "react-redux";
// app
import "./ProgramVoiceEditor.scss"
import {clone, create, destroy} from "../../store/actions/apiEntities";
import workspaceActivate from "../../store/actions/workspaceActivate";
import EntityAttributeField from "../entity/EntityAttributeField";
import makeWorkspaceSelector from "../../store/selectors/makeWorkspaceSelector";
import makeOrmEntityChildSelector from "../../store/selectors/makeOrmEntityChildSelector";
import ActionMenu from "../action/ActionMenu";
import {belongsTo, isSuccessful, optional} from "../../util";
import workspaceDeactivate from "../../store/actions/workspaceDeactivate";
import SubPanel from "../panel/SubPanel";
import EntityChildDropdownSelector from "../entity/EntityChildDropdownSelector";
import ProgramVoiceTrackEditor from "./ProgramVoiceTrackEditor";
import ActionButton from "../action/ActionButton";
import CreateIcon from "../icon/CreateIcon";
import makeWorkspaceActiveOrmEntitySelector from "../../store/selectors/makeWorkspaceActiveOrmEntitySelector";
import {NEW_PATTERN_ATTRIBUTES, NEW_TRACK_ATTRIBUTES} from "../../constants";


/**
 ProgramVoiceEditor component
 <p>
 @return {*}
 @param {{
   program Object,
   programVoice Object,
   programSequence Object (optional),
   pixelsPerBeat: Number,
 }} props for this component
 */
export default function ProgramVoiceEditor(props) {
  const
    programSequence = optional(props.programSequence),
    programVoice = props.programVoice,
    pixelsPerBeat = props.pixelsPerBeat,
    program = props.program;

  const
    tracksSelector = makeOrmEntityChildSelector("program voice track", "program voice", programVoice.id),
    tracks = useSelector(state => tracksSelector(state));

  const
    voicePanelWidthSelector = makeWorkspaceSelector("voicePanelWidth"),
    voicePanelWidth = useSelector(state => voicePanelWidthSelector(state));

  const
    activePatternSelector = makeWorkspaceActiveOrmEntitySelector(programVoice.id, "program sequence pattern"),
    activePattern = useSelector(state => activePatternSelector(state));

  const
    patternsSelector = makeOrmEntityChildSelector(
      "program sequence pattern",
      "program voice", programVoice.id,
      "program sequence", !!programSequence ? programSequence.id : null);

  const
    dispatch = useDispatch();

  const
    destroyVoice = () => {
      if (window.confirm("Delete Voice?"))
        dispatch(destroy("program voice", `Voice`, programVoice.id))
    };

  const
    cloneVoice = () => {
      dispatch(clone("program voice", `Voice`, programVoice.id))
    };

  const
    createTrack = () => {
      dispatch(create(
        "program voice track", NEW_TRACK_ATTRIBUTES,
        {
          ...belongsTo("program", program.id),
          ...belongsTo("program voice", programVoice.id),
        }
      ));
    };

  const
    activatePattern = (id) =>
      dispatch(workspaceActivate(programVoice.id, "program sequence pattern", id));

  const
    createPattern = () => {
      if (programSequence)
        dispatch(create(
          "program sequence pattern", NEW_PATTERN_ATTRIBUTES,
          {
            ...belongsTo("program", program.id),
            ...belongsTo("program voice", programVoice.id),
            ...belongsTo("program sequence", programSequence.id),
          }
        )).then((response) => activatePattern(response.payload.data.id));
    };

  const
    destroyPattern = () => {
      if (window.confirm("Delete Pattern?"))
        dispatch(destroy(
          "program sequence pattern", `Pattern`, activePattern.id
        )).then(
          (response) => {
            if (isSuccessful(response))
              dispatch(workspaceDeactivate(programVoice.id, "program sequence pattern"))
          })
    };

  const
    clonePattern = () => {
      dispatch(clone("program sequence pattern", `Pattern`, activePattern.id))
    };

  return (
    <div className="program-voice-editor">

      <div className="voice-attributes" style={{flexBasis: `${voicePanelWidth}px`}}>

        <div className="panels-column">
          <div className="panels-row">
            <ActionMenu className="voice-action-menu"
                        position="bottom left"
                        actions={[
                          {label: "Delete", action: destroyVoice},
                          {label: "Clone", action: cloneVoice},
                        ]}/>
            <EntityAttributeField className="program-voice-name" attrType="text" attrName="name"
                                  type="program voice" entity={programVoice}/>

          </div>
          <EntityAttributeField attrType="select" attrName="type" label="Voice"
                                attrOptions={["Percussive", "Melodic", "Harmonic", "Vocal"]}
                                type="program voice" entity={programVoice}/>
        </div>

        <SubPanel disabled={!programSequence}>
          {!!programSequence ? (
            <div className="panels-column">
              <div className="panels-row">

                <EntityChildDropdownSelector entitiesSelector={patternsSelector}
                                             format={o => `${o.name} (${o.type})`}
                                             className="active-pattern"
                                             active={activePattern}
                                             onActivate={activatePattern}/>

                <ActionMenu
                  className="active-pattern-action-menu"
                  position="bottom left"
                  actions={[
                    {label: "New Pattern", action: createPattern},
                    {label: "Delete", action: destroyPattern, disabled: !activePattern},
                    {label: "Clone", action: clonePattern, disabled: !activePattern},
                  ]}/>

                {!!activePattern ? (
                  <EntityAttributeField className="program-sequence-pattern-name" attrType="text" attrName="name"
                                        type="program sequence pattern" entity={activePattern}/>
                ) : (
                  <div className="none-selected">No Pattern</div>
                )}

              </div>
              {!!activePattern ? (
                <div className="panels-column">
                  <EntityAttributeField attrType="select" attrName="type" label="Pattern"
                                        attrOptions={["Loop", "Intro", "Outro"]}
                                        type="program sequence pattern" entity={activePattern}/>
                  <EntityAttributeField attrType="number" attrName="total" label="Total"
                                        type="program sequence pattern" entity={activePattern}/>
                </div>
              ) : ""}
            </div>
          ) : (
            <div className="none-selected">No Sequence</div>
          )}
        </SubPanel>
      </div>


      <div className="program-voice-tracks">
        {0 < tracks.length ? tracks.map((track) => (
          <ProgramVoiceTrackEditor key={track.id}
                                   programVoiceTrack={track}
                                   program={program}
                                   programSequence={programSequence}
                                   programSequencePattern={activePattern}
                                   programVoice={programVoice}
                                   pixelsPerBeat={pixelsPerBeat}/>
        )) : (
          <div key="create-program" className="new-program-voice-track">
            <ActionButton onClick={createTrack}><CreateIcon/></ActionButton>
          </div>
        )}
        {/* TODO edit program sequence pattern events */}
      </div>

    </div>
  );
};
