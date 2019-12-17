// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// vendor
import React from "react";
import {useDispatch} from "react-redux";
// app
import "./ProgramSequenceSelectorAttributeEditor.scss"
import EntityAttributeField from "../entity/EntityAttributeField";
import ActionMenu from "../action/ActionMenu";
import EntityChildDropdownSelector from "../entity/EntityChildDropdownSelector";
import makeOrmEntityChildSelector from "../../store/selectors/makeOrmEntityChildSelector";
import {clone, create, destroy} from "../../store/actions/apiEntities";
import {belongsTo, isSuccessful, optional} from "../../util";
import workspaceDeactivate from "../../store/actions/workspaceDeactivate";
import sendWorkspaceActivateSequence from "../../store/actions/sendWorkspaceActivateSequence";

//
const NEW_SEQUENCE_ATTRIBUTES = {
  name: "New",
  key: "C",
  tempo: 120.0,
  total: 16.0,
  density: 0.6,
};

/**
 ProgramSequenceSelectorAttributeEditor component
 <p>
 @return {*}
 @param {{
   program object,
   programVoices [object],
   activeSequence object (optional),
 }} props for this component
 */
export default function ProgramSequenceSelectorAttributeEditor(props) {
  const
    activeSequence = optional(props.activeSequence),
    program = props.program,
    voices = props.programVoices,
    dispatch = useDispatch();

  const
    sequencesSelector = makeOrmEntityChildSelector("program sequence", "program", program.id);

  const
    createSequence = () =>
      dispatch(create(
        "program sequence", NEW_SEQUENCE_ATTRIBUTES,
        {...belongsTo("program", program.id)}
      )).then(
        (response) => sendWorkspaceActivateSequence(dispatch, program.id, response.payload.data.id, voices));

  const
    cloneSequence = () => {
      dispatch(clone(
        "program sequence", `Sequence "${activeSequence.name}"`, activeSequence.id,
        {
          ...NEW_SEQUENCE_ATTRIBUTES,
          name: `Clone of ${activeSequence.name}`,
        },
        {...belongsTo("program", program.id)}
      )).then(
        (response) => sendWorkspaceActivateSequence(dispatch, program.id, response.payload.data.id, voices));
    };

  const
    destroySequence = () => {
      if (window.confirm("Delete Sequence?"))
        dispatch(destroy(
          "program sequence", `Sequence "${activeSequence.name}"`, activeSequence.id
        )).then(
          (response) => {
            if (isSuccessful(response))
              dispatch(workspaceDeactivate(program.id, "program sequence"))
          })
    };

  const
    activateSequence = (id) => sendWorkspaceActivateSequence(dispatch, program.id, id, voices);


  return (
    <div className="program-sequence-selector-attribute-editor">

      {/*<EntityChildSelector entitiesSelector={sequencesSelector}
                                     textAttr="name" label="Sequence"
                                     className="active-sequence"
                                     activeId={activeSequenceId}
                                     onActivate={activateSequence}/>*/}

      <EntityChildDropdownSelector entitiesSelector={sequencesSelector}
                                   format={o => o.name}
                                   className="active-sequence"
                                   active={activeSequence}
                                   onActivate={activateSequence}/>

      <ActionMenu
        className="active-sequence-action-menu"
        position="bottom left"
        actions={[
          {label: "New Sequence", action: createSequence},
          {label: "Delete", action: destroySequence, disabled: !activeSequence},
          {label: "Clone", action: cloneSequence, disabled: !activeSequence},
        ]}/>

      {activeSequence ? "" : <div className="none-selected">No Sequence</div>}

      <EntityAttributeField attrType="text"
                            attrName="name"
                            type="program sequence"
                            entity={activeSequence}/>

      <EntityAttributeField attrType="number"
                            attrName="total"
                            label="Total"
                            type="program sequence"
                            entity={activeSequence}/>

      <EntityAttributeField attrType="text"
                            attrName="key"
                            label="Key"
                            type="program sequence"
                            entity={activeSequence}/>

      <EntityAttributeField attrType="number"
                            attrName="tempo"
                            label="Tempo"
                            type="program sequence"
                            entity={activeSequence}/>

      <EntityAttributeField attrType="number"
                            attrName="density"
                            label="Density"
                            type="program sequence"
                            entity={activeSequence}/>

    </div>
  );
};
