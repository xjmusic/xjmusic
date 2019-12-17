// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// vendor
import React from "react";
import {useSelector} from "react-redux";
// app
import "./ProgramSequenceTrackEditor.scss"
import makeWorkspaceSelector from "../../store/selectors/makeWorkspaceSelector";
import {wrapSvgInCssImageDataUrl} from "../../util";
import {
  BASE_PIXELS_PER_BEAT,
  BG_BAR_START_LINE_COLOR,
  BG_BAR_TICK_LINE_COLOR,
  BG_CENTER_LINE_COLOR,
  BG_CENTER_LINE_DASH_ARRAY,
  BG_HEIGHT,
  BG_STROKE_LINE_CAP,
  BG_STROKE_WIDTH
} from "../../constants";

/**
 ProgramSequenceTrackEditor component
 <p>
 @return {*}
 @param {{
   children Object,
   programSequence Object,
 }} props for this component
 */
export default function ProgramSequenceTrackEditor(props) {
  const
    programSequence = props.programSequence;

  const
    timelineGridSelector = makeWorkspaceSelector("timelineGrid"),
    timelineGrid = useSelector(state => timelineGridSelector(state));

  const
    zoomLevelSelector = makeWorkspaceSelector("zoomLevel"),
    zoomLevel = useSelector(state => zoomLevelSelector(state));

  // without a program sequence, return a placeholder
  if (!programSequence) return (
    <div className="program-sequence-track-editor"
         style={{flexBasis: `${0}px`}}>
      {props.children}
    </div>
  );

  const trackSequenceWidth = BASE_PIXELS_PER_BEAT * programSequence.total * zoomLevel;
  const barSize = 4; // TODO support divisions by bar sizes other than 4
  const barWidth = BASE_PIXELS_PER_BEAT * barSize * zoomLevel;

  let shapes = [];
  for (let b = 0; b < barSize; b += barSize * timelineGrid)
    shapes.push(<rect key={`rect-${Math.floor(b * 100000)}`} x={b * BASE_PIXELS_PER_BEAT * zoomLevel} y={0}
                      fill={0 === b ? BG_BAR_START_LINE_COLOR : BG_BAR_TICK_LINE_COLOR}
                      width={BG_STROKE_WIDTH} height={BG_HEIGHT}/>);
  shapes.push(<line key="line" x1={1} y1={BG_HEIGHT / 2}
                    x2={barWidth} y2={BG_HEIGHT / 2}
                    stroke={BG_CENTER_LINE_COLOR}
                    strokeWidth={BG_STROKE_WIDTH} strokeLinecap={BG_STROKE_LINE_CAP}
                    strokeDasharray={BG_CENTER_LINE_DASH_ARRAY}/>);
  const trackBackgroundSVG = wrapSvgInCssImageDataUrl(
    <svg xmlns='http://www.w3.org/2000/svg' width={barWidth} height={BG_HEIGHT}>{shapes}</svg>
  );

  return (
    <div className="program-sequence-track-editor"
         style={{backgroundImage: trackBackgroundSVG, flexBasis: `${trackSequenceWidth}px`}}>
      {props.children}
    </div>
  );
};
