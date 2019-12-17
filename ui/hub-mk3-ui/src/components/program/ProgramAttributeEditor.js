// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// vendor
import React from "react";
// app
import "./ProgramAttributeEditor.scss"
import EntityMemeEditor from "../entity/EntityMemeEditor";
import EntityAttributeField from "../entity/EntityAttributeField";

/**
 ProgramAttributeEditor component
 <p>
 @return {*}
 @param {{
   program Object,
   children Object,
 }} props for this component
 */
export default function ProgramAttributeEditor(props) {
  const
    program = props.program;

  return (
    <div className="program-attribute-editor">

      {props.children}

      <EntityAttributeField attrType="text"
                            attrName="name" label="Program"
                            type="programs"
                            entity={program}/>

      <EntityAttributeField attrType="select"
                            attrName="type" label="Type"
                            attrOptions={["Macro", "Main", "Rhythm", "Detail"]}
                            type="programs"
                            entity={program}/>

      <EntityAttributeField attrType="select"
                            attrName="state" label="State"
                            attrOptions={["Draft", "Published"]}
                            type="programs"
                            entity={program}/>

      <EntityAttributeField attrType="text"
                            attrName="key" label="Key"
                            type="programs"
                            entity={program}/>

      <EntityAttributeField attrType="number"
                            attrName="tempo" label="Tempo"
                            type="programs"
                            entity={program}/>

      <EntityAttributeField attrType="number"
                            attrName="density" label="Density"
                            type="programs"
                            entity={program}/>

      <div className="entity-attribute field memes">
        <label>Memes</label>
        <EntityMemeEditor type="program meme"
                          parentType="program"
                          parentId={program.id}/>
      </div>

    </div>
  );
};
