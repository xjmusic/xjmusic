// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// vendor
import React from "react";
import {useSelector} from "react-redux";
// app
import "./ProgramSequenceTimelineNavigator.scss"
import makeWorkspaceSelector from "../../store/selectors/makeWorkspaceSelector";
import WorkspaceOptionSelector from "../panel/WorkspaceOptionSelector";
import ProgramSequenceSelectorAttributeEditor from "./ProgramSequenceSelectorAttributeEditor";
import {optional} from "../../util";
import {TIMELINE_GRID_OPTIONS, ZOOM_LEVEL_OPTIONS} from "../../constants";

/**
 ProgramSequenceTimelineNavigator component
 <p>
 @return {*}
 @param {{
   program object,
   programVoices [Object],
   activeSequence object (optional),
 }} props for this component
 */
export default function ProgramSequenceTimelineNavigator(props) {
  const
    program = props.program,
    voices = props.programVoices,
    activeSequence = optional(props.activeSequence,);

  const
    voicePanelWidthSelector = makeWorkspaceSelector("voicePanelWidth"),
    firstPanelWidth = useSelector(state => voicePanelWidthSelector(state));

  const
    timelineGridSelector = makeWorkspaceSelector("timelineGrid"),
    timelineGrid = useSelector(state => timelineGridSelector(state));

  const
    zoomLevelSelector = makeWorkspaceSelector("zoomLevel"),
    zoomLevel = useSelector(state => zoomLevelSelector(state));

  const
    trackPanelWidthSelector = makeWorkspaceSelector("trackPanelWidth"),
    secondPanelWidth = useSelector(state => trackPanelWidthSelector(state));

  return (
    <div className="program-sequence-timeline-navigator">

      <div className="panel" style={{flexBasis: `${firstPanelWidth}px`}}>
        <WorkspaceOptionSelector value={timelineGrid}
                                 label="Grid"
                                 name="timelineGrid"
                                 options={TIMELINE_GRID_OPTIONS}/>
        <WorkspaceOptionSelector value={zoomLevel}
                                 label="Zoom"
                                 name="zoomLevel"
                                 options={ZOOM_LEVEL_OPTIONS}/>
      </div>

      <div className="panel" style={{flexBasis: `${secondPanelWidth}px`}}>
        <label>Sequence</label>
      </div>

      <ProgramSequenceSelectorAttributeEditor program={program}
                                              activeSequence={activeSequence}
                                              programVoices={voices}/>

    </div>
  );
};
