/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// vendor
import React, {useState} from "react";
import {useDispatch, useSelector} from "react-redux";
// app
import "./ProgramSequenceBindingEditor.scss"
import {create, destroy} from "../../store/actions/apiEntities";
import {belongsTo, computeMax, computeSeriesFromZeroTo} from "../../util";
import makeOrmEntityChildSelector from "../../store/selectors/makeOrmEntityChildSelector";
import Popup from "reactjs-popup";
import EntityChildSelector from "../entity/EntityChildSelector";
import EntityMemeEditor from "../entity/EntityMemeEditor";
import DestroyIcon from "../icon/DestroyIcon";
import sendWorkspaceActivateSequence from "../../store/actions/sendWorkspaceActivateSequence";

/**
 ProgramSequenceBindingEditor component
 <p>
 @return {*}
 @typedef Label{string} of attribute to display next to field INSTEAD of a program sequenceBinding editor row
 @typedef ActionFunction{function} to callback when button is clicked INSTEAD of a program sequenceBinding editor row
 @param {{
   program Object,
   activeSequence Object,
   programVoices [Object],
 }} props for this component
 */
export default function ProgramSequenceBindingEditor(props) {
  const
    dispatch = useDispatch(),
    program = props.program,
    voices = props.programVoices,
    activeSequence = props.activeSequence;

  const
    programSequencesSelector = makeOrmEntityChildSelector("program sequence", "program", program.id),
    programSequences = useSelector(state => programSequencesSelector(state)),
    nameForProgramSequenceId = programSequences.reduce((obj, ps) => {
      obj[ps.id] = ps.name;
      return obj;
    }, {});

  const
    programSequenceBindingsSelector = makeOrmEntityChildSelector("program sequence binding", "program", program.id),
    programSequenceBindings = useSelector(state => programSequenceBindingsSelector(state)),
    psbOffsets = computeSeriesFromZeroTo(0 < programSequenceBindings.length ? computeMax(programSequenceBindings.map(b => b.offset)) + 1 : 1),
    programSequenceBindingsAtOffset = psbOffsets.reduce((obj, offset) => {
      obj[offset] = programSequenceBindings.filter(psb => psb.offset === offset)
        .reduce((arr, psb) => {
          arr.push(psb);
          return arr;
        }, []);
      return obj;
    }, {});

  const
    activateSequence = (id) => sendWorkspaceActivateSequence(dispatch, program.id, id, voices);

  const
    destroySequenceBinding = (psb) =>
      dispatch(destroy("program sequence binding", `binding at offset ${psb.offset}`, psb.id)),
    createSequenceBinding = (programSequenceId, offset) => {
      dispatch(create(
        "program sequence binding", {offset},
        {
          ...belongsTo("program", program.id),
          ...belongsTo("program sequence", programSequenceId),
        }
      ));
    },
    [getIsCreateBindingModalOpen, setIsCreateBindingModalOpen] = useState(false),
    [getCreateBindingModalOffset, setCreateBindingModalOffset] = useState(0),
    openSequenceBindingCreateModal = (offset) => {
      setCreateBindingModalOffset(offset);
      setIsCreateBindingModalOpen(true);
    },
    submitSequenceBindingCreateModal = (programSequenceId) => {
      createSequenceBinding(programSequenceId, getCreateBindingModalOffset);
      setIsCreateBindingModalOpen(false);
    };

  return (
    <div className="program-sequence-binding-editor">

      <div className="offset-group">
        <div className="header">OFFSET</div>
        <div className="name">SEQUENCES +&nbsp;MEMES</div>
        <Popup open={getIsCreateBindingModalOpen}
               onClose={() => setIsCreateBindingModalOpen(false)}>
          <EntityChildSelector entitiesSelector={programSequencesSelector}
                               textAttr="name"
                               className="active-pattern"
                               onActivate={submitSequenceBindingCreateModal}
                               autoFocus
                               menuIsOpen/>
        </Popup>
      </div>

      {psbOffsets.map(offset => (
        <div key={`offset-group-${offset}`} className="offset-group">
          <div className="header">{offset}</div>
          {programSequenceBindingsAtOffset[offset]
            .map((psb) => (
              <div
                className={`binding ${activeSequence && (activeSequence.id === psb.programSequenceId) ? "active" : ""}`}
                key={psb.id}
                onClick={() => activateSequence(psb.programSequenceId)}>
                <EntityMemeEditor label={nameForProgramSequenceId[psb.programSequenceId]}
                                  type="program sequence binding meme"
                                  parentType="program sequence binding"
                                  parentId={psb.id}
                                  createWithRelationships={belongsTo("program", program.id)}/>
                <div className="destroy" onClick={() => destroySequenceBinding(psb)}><DestroyIcon/></div>
              </div>
            ))}
          <div className="new-binding" onClick={() => openSequenceBindingCreateModal(offset)}>+</div>
        </div>
      ))}

    </div>
  );
};
