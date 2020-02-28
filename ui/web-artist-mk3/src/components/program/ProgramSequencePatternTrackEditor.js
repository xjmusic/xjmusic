/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// vendor
import React from "react";
import {useSelector} from "react-redux";
// app
import "./ProgramSequencePatternTrackEditor.scss"
import makeWorkspaceSelector from "../../store/selectors/makeWorkspaceSelector";
import ProgramSequencePatternEvent from "./ProgramSequencePatternEvent";
import makeOrmEntityChildSelector from "../../store/selectors/makeOrmEntityChildSelector";
import {BASE_PIXELS_PER_BEAT} from "../../constants";

/**
 ProgramSequencePatternTrackEditor component
 <p>
 @return {*}
 @param {{
   children Object,
   program Object,
   programVoice Object,
   programVoiceTrack Object,
   programSequence Object,
   programSequencePattern Object(optional),
   pixelsPerBeat: Number,
 }} props for this component
 */
export default function ProgramSequencePatternTrackEditor(props) {
  const
    // programId = props.programId,
    // programVoiceId = props.programVoiceId,
    programVoiceTrack = props.programVoiceTrack,
    // programSequenceId = props.programSequenceId,
    pixelsPerBeat = props.pixelsPerBeat,
    programSequencePattern = props.programSequencePattern;

  const
    eventsSelector = makeOrmEntityChildSelector("program sequence pattern event",
      "program sequence pattern", programSequencePattern.id,
      "program voice track", programVoiceTrack.id),
    events = useSelector(state => eventsSelector(state));

  const
    zoomLevelSelector = makeWorkspaceSelector("zoomLevel"),
    zoomLevel = useSelector(state => zoomLevelSelector(state));

  const trackPatternWidth = !!programSequencePattern ? BASE_PIXELS_PER_BEAT * programSequencePattern.total * zoomLevel : 0;


  return (
    <div className="program-sequence-pattern-track-editor" style={{flexBasis: `${trackPatternWidth}px`}}>
      {events.map((event) => (
        <ProgramSequencePatternEvent key={event.id}
                                     event={event}
                                     pixelsPerBeat={pixelsPerBeat}/>
      ))}
      {/* TODO edit program sequence pattern events */}
    </div>
  );
};
