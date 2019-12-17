// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// vendor
import React from "react";
import {useDispatch, useSelector} from "react-redux";
// app
import "./ProgramSequenceChordsEditor.scss"
import {create} from "../../store/actions/apiEntities";
import workspaceActivate from "../../store/actions/workspaceActivate";
import makeWorkspaceSelector from "../../store/selectors/makeWorkspaceSelector";
import makeOrmEntityChildSelector from "../../store/selectors/makeOrmEntityChildSelector";
import {belongsTo, visibleIf} from "../../util";
import ProgramSequenceTrackEditor from "./ProgramSequenceTrackEditor";
import ActionButton from "../action/ActionButton";
import CreateIcon from "../icon/CreateIcon";
import ProgramSequenceChord from "./ProgramSequenceChord";
import {NEW_CHORD_ATTRIBUTES} from "../../constants";


/**
 ProgramSequenceChordsEditor component to edit the chords of a sequence
 <p>
 @return {*}
 @typedef EntityId{String} of entity to edit, or will render an empty editor if no sequence is selected
 @param {{
   program Object,
   pixelsPerBeat: Number,
   programSequence Object (optional),
 }} props for this component
 */
export default function ProgramSequenceChordsEditor(props) {
  const
    programSequence = props.programSequence,
    program = props.program,
    pixelsPerBeat = props.pixelsPerBeat,
    dispatch = useDispatch();

  const
    voicePanelWidthSelector = makeWorkspaceSelector("voicePanelWidth"),
    voicePanelWidth = useSelector(state => voicePanelWidthSelector(state));

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
  //   sequencePanelWidthSelector = makeWorkspaceSelector("sequencePanelWidth"),
  //   sequencePanelWidth = useSelector(state => sequencePanelWidthSelector(state));

  // const
  //   activeChordIdSelector = makeWorkspaceActiveSelector(programSequence.id, "program sequence chord"),
  //   activeChordId = useSelector(state => activeChordIdSelector(state));

  const
    chordsSelector = makeOrmEntityChildSelector(
      "program sequence chord",
      "program sequence",
      !!programSequence ? programSequence.id : null),
    chords = useSelector(state => chordsSelector(state));

  // without a program sequence, return a placeholder
  if (!programSequence) return (
    <div className="program-sequence-chords-editor">
      <div className="none-selected">No Sequence</div>
    </div>
  );

  const
    activateChord = (id) =>
      dispatch(workspaceActivate(programSequence.id, "program sequence chord", id));

  const
    createChord = () => {
      dispatch(create(
        "program sequence chord", NEW_CHORD_ATTRIBUTES,
        {
          ...belongsTo("program", program.id),
          ...belongsTo("program sequence", programSequence.id),
        }
      )).then((response) => activateChord(response.payload.data.id));
    };

  return (
    <div className="program-sequence-chords-editor" style={{flexBasis: `${trackHeight}px`}}>
      <div className="voice-attributes" style={{flexBasis: `${voicePanelWidth}px`}}>
        <div className="panels-column">
          <div className="panels-row">
            <label>Sequence Chords</label>
          </div>
        </div>
      </div>
      <div className="track-attributes" style={{flexBasis: `${trackPanelWidth}px`}}>

        <div className="panels-column">
          <div className="panels-row">
            <div key="create-chord" className="new-sequence-voice">
              <ActionButton onClick={createChord}><CreateIcon/></ActionButton>
            </div>
          </div>
        </div>
      </div>

      {visibleIf(!!programSequence,
        <ProgramSequenceTrackEditor program={program}
                                    programSequence={programSequence}>
          {chords.map((chord) => (
            <ProgramSequenceChord key={chord.id}
                                  chord={chord}
                                  pixelsPerBeat={pixelsPerBeat}/>
          ))}
        </ProgramSequenceTrackEditor>
      )}

    </div>
  );
};
