// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// vendor
import React from "react";
import {useDispatch, useSelector} from "react-redux";
// app
import "./EntityMemeEditor.scss"
import EntityAttributeField from "./EntityAttributeField";
import makeOrmEntityChildSelector from "../../store/selectors/makeOrmEntityChildSelector";
import {create, destroy} from "../../store/actions/apiEntities";
import {belongsTo, optional} from "../../util";
import CreateIcon from "../icon/CreateIcon";
import DestroyIcon from "../icon/DestroyIcon";

//
const NEW_MEME_ATTRIBUTES = {
  name: "New",
};

/**
 EntityMemeEditor to show/create/delete memes of a given type for a given parent

 @return {*}
 @typedef EntityType{string} of meme entity to show / create / delete
 @typedef EntityId{string} is a UUID
 @typedef ClassName{string} of attribute to edit
 @typedef EntityRelationships{object} (optional) to include when creating a new meme
 @typedef Label{string} to add inside field
 @param {{
    type EntityType,
    parentType EntityType,
    parentId EntityId,
    label Label (optional),
    className ClassName (optional),
    createWithRelationships EntityRelationships (optional),
 }} props describing the child selector to build
 */
export default function EntityMemeEditor(props) {
  const
    dispatch = useDispatch();

  const
    memesSelector = makeOrmEntityChildSelector(props.type, props.parentType, props.parentId),
    memes = useSelector(state => memesSelector(state));

  const
    destroyMeme = (meme) =>
      dispatch(destroy(props.type, `meme "${meme.name}"`, meme.id)),
    createMeme = () => {
      dispatch(create(
        props.type, NEW_MEME_ATTRIBUTES,
        {
          ...optional(props.createWithRelationships, {}),
          ...belongsTo(props.parentType, props.parentId),
        }
      ));
    };

  return (
    <div className={`entity-meme-editor field ${optional(props.className, "")}`}>
      {props.label ? (<div className="name">{props.label}</div>) : ""}

      <div className="meme">
        <div className="create" onClick={() => createMeme()}><CreateIcon scale={0.6}/></div>
      </div>
      {memes.map((meme) => (
        <div key={meme.id} className="meme">
          <EntityAttributeField attrType="text"
                                attrName="name"
                                type={props.type}
                                entity={meme}/>

          <div className="destroy" onClick={() => destroyMeme(meme)}><DestroyIcon scale={0.6}/></div>
        </div>
      ))}

    </div>
  );
};

