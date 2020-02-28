/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// vendor
import React, {useState} from "react";
import {useDispatch, useSelector} from "react-redux";
// app
import "./ProgramEditor.scss"
import {create, read} from "../../store/actions/apiEntities";
import workspaceActivate from "../../store/actions/workspaceActivate";
import makeWorkspaceActiveSelector from "../../store/selectors/makeWorkspaceActiveSelector";
import {belongsTo, isSuccessful, visibleIf} from "../../util";
import makeOrmEntityChildSelector from "../../store/selectors/makeOrmEntityChildSelector";
import makeOrmEntitySelector from "../../store/selectors/makeOrmEntitySelector";
import ProgramSequenceBindingEditor from "./ProgramSequenceBindingEditor";
import WorkspaceSwitcher from ".././panel/WorkspaceSwitcher";
import ProgramVoiceEditor from "./ProgramVoiceEditor";
import ActionButton from "../action/ActionButton";
import CreateIcon from "../icon/CreateIcon";
import ProgramAttributeEditor from "./ProgramAttributeEditor";
import ProgramSequenceTimelineNavigator from "./ProgramSequenceTimelineNavigator";
import ProgramSequenceChordsEditor from "./ProgramSequenceChordsEditor";
import makeWorkspaceActiveOrmEntitySelector from "../../store/selectors/makeWorkspaceActiveOrmEntitySelector";
import {BASE_PIXELS_PER_BEAT, NEW_VOICE_ATTRIBUTES,} from "../../constants";
import makeWorkspaceComputationSelector from "../../store/selectors/makeWorkspaceComputationSelector";

const
  PROGRAM_STATUS_INITIAL = "INITIAL",
  PROGRAM_STATUS_LOADING = "LOADING",
  PROGRAM_STATUS_OK = "OK",
  PROGRAM_STATUS_FAILED = "FAILED",
  //
  PROGRAM_TYPE_MACRO = "Macro",
  PROGRAM_TYPE_MAIN = "Main",
  PROGRAM_TYPE_DETAIL = "Detail",
  PROGRAM_TYPE_RHYTHM = "Rhythm",
  //
  WORKSPACE_SEQUENCE_MODE = "WORKSPACE_SEQUENCE_MODE",
  BINDING_MODE = "BINDING",
  EDITING_MODE = "EDITING",
  WORKSPACE_SEQUENCE_MODE_INITIAL = {
    [PROGRAM_TYPE_MACRO]: BINDING_MODE,
    [PROGRAM_TYPE_MAIN]: EDITING_MODE,
    [PROGRAM_TYPE_DETAIL]: EDITING_MODE,
    [PROGRAM_TYPE_RHYTHM]: EDITING_MODE,
  },
  //
  API_READ_PROGRAM_INCLUDE_ENTITIES = [
    "program-memes",
    "program-sequences",
    "program-sequence-bindings",
    "program-sequence-binding-memes",
    "program-sequence-chords",
    "program-sequence-patterns",
    "program-sequence-pattern-events",
    "program-voices",
    "program-voice-tracks",
  ].join(",");

/**
 ProgramEditor component
 <p>
 We do the API fetch of the program and included entities here, which makes assumptions about
 all the necessary child entities coming back with it. Child components assume that they should
 work with whatever is already in the Redux ORM store.
 <p>
 @return {*}
 @typedef EntityId{String} of entity to edit
 @param {{
   programId EntityId,
 }} props for this component
 */
export default function ProgramEditor(props) {
  const
    dispatch = useDispatch(),
    programId = props.programId,
    [programStatus, setProgramStatus] = useState(PROGRAM_STATUS_INITIAL),
    programSelector = makeOrmEntitySelector("program", programId),
    program = useSelector(state => programSelector(state));

  const
    activeSequenceSelector = makeWorkspaceActiveOrmEntitySelector(programId, "program sequence"),
    activeSequence = useSelector(state => activeSequenceSelector(state));

  const
    activeSequenceModeSelector = makeWorkspaceActiveSelector(programId, WORKSPACE_SEQUENCE_MODE),
    activeSequenceMode = useSelector(state => activeSequenceModeSelector(state));

  const
    voicesSelector = makeOrmEntityChildSelector("program voice", "program", programId),
    voices = useSelector(state => voicesSelector(state));

  const
    pixelsPerBeatSelector = makeWorkspaceComputationSelector("zoomLevel", z => BASE_PIXELS_PER_BEAT * z),
    pixelsPerBeat = useSelector(state => pixelsPerBeatSelector(state));

  const
    createVoice = () =>
      dispatch(create(
        "program voice", NEW_VOICE_ATTRIBUTES,
        {...belongsTo("program", programId)}
      ));

  // state machine
  switch (programStatus) {

    // only hits initial state once, dispatches the read program action and sets status to loading
    case PROGRAM_STATUS_INITIAL:
    default:
      dispatch(read("program", programId, API_READ_PROGRAM_INCLUDE_ENTITIES))
        .then((response => {
          if (!isSuccessful(response))
            setProgramStatus(PROGRAM_STATUS_FAILED);
        }));
      setProgramStatus(PROGRAM_STATUS_LOADING);
      return (
        <div className="program-editor">
          <div className="header">
            <label>Please wait...</label>
          </div>
        </div>
      );

    //
    case PROGRAM_STATUS_LOADING:
      if (!!program) {
        dispatch(workspaceActivate(programId, WORKSPACE_SEQUENCE_MODE, WORKSPACE_SEQUENCE_MODE_INITIAL[program.type]));
        setProgramStatus(PROGRAM_STATUS_OK);
      }
      return (
        <div className="program-editor">
          <div className="header">
            <label>Loading...</label>
          </div>
        </div>
      );

    //
    case PROGRAM_STATUS_FAILED:
      return (
        <div className="program-editor">
          <div className="header">
            <label>Failed!</label>
          </div>
        </div>
      );

    //
    case PROGRAM_STATUS_OK:
      return (
        <div className="program-editor">

          {/* Always visible at top, with workspace switcher injected */}
          <ProgramAttributeEditor program={program}>
            <WorkspaceSwitcher
              parentId={programId}
              type={WORKSPACE_SEQUENCE_MODE}
              value={activeSequenceMode}
              options={[
                {label: "Bind", value: BINDING_MODE},
                {label: "Edit", value: EDITING_MODE, disabled: PROGRAM_TYPE_MACRO === program.type},
              ]}/>
          </ProgramAttributeEditor>

          {/* Always display sequence Selector */}
          <ProgramSequenceTimelineNavigator program={program}
                                            programVoices={voices}
                                            activeSequence={activeSequence}/>

          <div className="program-sequence-editor">

            {/* Sequence Chord Editor only visible for main program type */
              visibleIf(PROGRAM_TYPE_MAIN === program.type,
                <ProgramSequenceChordsEditor
                  program={program}
                  programSequence={activeSequence}
                  pixelsPerBeat={pixelsPerBeat}/>
              )}

            {/* Sequence Binding Editor only visible in bindings mode */
              visibleIf(BINDING_MODE === activeSequenceMode,
                <ProgramSequenceBindingEditor
                  activeSequence={activeSequence}
                  programVoices={voices}
                  program={program}/>)}

            {/* List of voice editors only visible in editing mode for a rhythm-type program */
              visibleIf(EDITING_MODE === activeSequenceMode && PROGRAM_TYPE_RHYTHM === program.type,
                voices
                  .map((voice) => (
                    <ProgramVoiceEditor key={voice.id}
                                        program={program}
                                        programSequence={activeSequence}
                                        programVoice={voice}
                                        pixelsPerBeat={pixelsPerBeat}/>))
                  .concat([
                    <div key="create-program" className="new-program-voice">
                      <ActionButton onClick={createVoice}><CreateIcon/></ActionButton>
                    </div>]))}

          </div>
        </div>
      );
  }
};
