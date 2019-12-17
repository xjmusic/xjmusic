// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// vendor
import React from "react";
import {useDispatch, useSelector} from "react-redux";
// app
import "./ProgramVoiceTrackEditor.scss"
import {create, destroy} from "../../store/actions/apiEntities";
import makeWorkspaceSelector from "../../store/selectors/makeWorkspaceSelector";
import {belongsTo, optional, visibleIf} from "../../util";
import EntityAttributeField from "../entity/EntityAttributeField";
import ActionMenu from "../action/ActionMenu";
import ProgramSequenceTrackEditor from "./ProgramSequenceTrackEditor";
import ProgramSequencePatternTrackEditor from "./ProgramSequencePatternTrackEditor";
import ActionButton from "../action/ActionButton";
import CreateIcon from "../icon/CreateIcon";
import {NEW_EVENT_ATTRIBUTES, NEW_TRACK_ATTRIBUTES} from "../../constants";


/**
 ProgramVoiceTrackEditor component
 <p>
 @return {*}
 @param {{
   program Object ,
   programVoice Object ,
   programVoiceTrack Object ,
   programSequencePattern Object (optional),
   programSequence Object (optional),
   pixelsPerBeat: Number,
 }} props for this component
 */
export default function ProgramVoiceTrackEditor(props) {
  const
    programSequencePattern = optional(props.programSequencePattern),
    programSequence = optional(props.programSequence),
    programVoiceTrack = props.programVoiceTrack,
    programVoice = props.programVoice,
    pixelsPerBeat = props.pixelsPerBeat,
    program = props.program;

  const
    trackPanelWidthSelector = makeWorkspaceSelector("trackPanelWidth"),
    trackPanelWidth = useSelector(state => trackPanelWidthSelector(state));

  const
    trackHeightSelector = makeWorkspaceSelector("trackHeight"),
    trackHeight = useSelector(state => trackHeightSelector(state));

  // const
  //   timelineGridSelector = makeWorkspaceSelector("timelineGrid"),
  //   timelineGrid = useSelector(state => timelineGridSelector(state));

  // const
  //   zoomLevelSelector = makeWorkspaceSelector("zoomLevel"),
  //   zoomLevel = useSelector(state => zoomLevelSelector(state));

  const
    dispatch = useDispatch();

  const
    destroyTrack = () => {
      if (window.confirm("Delete Track?"))
        dispatch(destroy("program voice track", `Track`, programVoiceTrack.id))
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
    createEvent = () => {
      if (!!programSequencePattern)
        dispatch(create(
          "program sequence pattern event", NEW_EVENT_ATTRIBUTES,
          {
            ...belongsTo("program", program.id),
            ...belongsTo("program voice track", programVoiceTrack.id),
            ...belongsTo("program sequence pattern", programSequencePattern.id),
          }
        ));
    };

  return (
    <div className="program-voice-track-editor" style={{flexBasis: `${trackHeight}px`}}>

      <div className="track-attributes" style={{flexBasis: `${trackPanelWidth}px`}}>

        <div className="panels-column">
          <div className="panels-row">
            <ActionMenu className="track-action-menu"
                        position="bottom left"
                        actions={[
                          {label: "New Track", action: createTrack},
                          {label: "Delete", action: destroyTrack},
                        ]}/>
            <EntityAttributeField className="track-name" attrType="text" attrName="name"
                                  type="program voice track" entity={programVoiceTrack}/>
          </div>
          {visibleIf(!!programSequencePattern,
            <div className="panels-row new-sequence-pattern-event">
              <div key="create-event">
                <ActionButton onClick={createEvent}><CreateIcon/></ActionButton>
              </div>
            </div>)}
        </div>
      </div>

      {visibleIf(!!programSequence,
        <ProgramSequenceTrackEditor programSequence={programSequence}>
          {visibleIf(!!programSequencePattern,
            <ProgramSequencePatternTrackEditor
              program={program}
              programVoice={programVoice}
              programVoiceTrack={programVoiceTrack}
              programSequence={programSequence}
              programSequencePattern={programSequencePattern}
              pixelsPerBeat={pixelsPerBeat}/>
          )}
        </ProgramSequenceTrackEditor>
      )}

    </div>
  );
};
