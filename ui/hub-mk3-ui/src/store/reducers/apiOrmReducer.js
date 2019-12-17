// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// app
import deserialize from "../jsonapi/deserialize"
import orm from "../orm"
import {toEntityName} from "../../util";

// Constants to identify requests handled by the api ORM Reducer
export const API_ORM_REQUEST = "API_ORM_REQUEST";
export const API_ORM_SUCCESS = "API_ORM_SUCCESS";
export const API_ORM_FAILURE = "API_ORM_FAILURE";
export const API_ORM_DELETED = "API_ORM_DELETED";
export const API_ORM_CLONED = "API_ORM_CLONED";

/**
 Add an entity payload to a map of entities by type.
 Ensure that the entitiesByType map has a key of the given entity type, containing an array of entities of that type

 @param entitiesByType to which entity will be added the array at the key of its type
 @param type of entity to add
 @param entity to add
 */
function addEntityByType(entitiesByType, type, entity) {
  if (!(type in entitiesByType)) entitiesByType[type] = [];
  entitiesByType[type].push(entity);
}

/**
 Update an entity using the given ORM model with the given payload data

 @param model ORM model to use to manipulate state
 @param payload data to update model with
 */
function createOrUpdateOne(model, payload) {
  if (model.idExists(payload.id))
    model.withId(payload.id).update(payload);
  else
    model.create(payload);
}

/**
 Remove a model that has been deleted

 @param model ORM model to use to manipulate state
 @param id of model to remove
 */
function cleanup(model, id) {
  if (model.idExists(id))
    model.withId(id).delete();
}

/**
 Update all entities of a given type (from an entitiesByType map) using the given ORM model

 @param entitiesByType to which entity will be added the array at the key of its type
 @param type of entity to add
 @param model ORM to manipulate state
 */
function createOrUpdateAll(entitiesByType, type, model) {
  if (!(type in entitiesByType) || !Array.isArray(entitiesByType[type])) return;

  for (let i = 0; i < entitiesByType[type].length; i++)
    createOrUpdateOne(model, entitiesByType[type][i]);
}

/**
 * Get ORM model for a specified type, given the session
 * @param sess ORM session to get model from
 * @param type of model to get
 * @returns {Model}
 */
function modelFor(sess, type) {
  return sess[toEntityName(type)];
}

/**
 * The ORM reducer that processes an API response, deserializes the data, and updates state via ORM models
 * @param baseState to update
 * @param action to call next
 * @return {*} updated state
 */
const apiOrmReducer = function (baseState, action) {
  const state = !!baseState ? baseState : orm.getEmptyState();

  // ORM Session from slice of redux state-- but if it's a new state, create from empty
  // Session-specific Models are available as properties on the Session instance.
  const sess = orm.session(state);

  // only handle certain actions
  switch (action.type) {
    case API_ORM_SUCCESS:

      // create an entity map of entity type -> array of entities of that type
      let entitiesByType = {};

      // determine if data is array-- if array then read many, else read one into the entity map
      if (action.payload && 'data' in action.payload)
        if (Array.isArray(action.payload.data))
          for (let i = 0; i < action.payload.data.length; i++)
            addEntityByType(entitiesByType, action.payload.data[i].type, deserialize(action.payload.data[i]));
        else
          addEntityByType(entitiesByType, action.payload.data.type, deserialize(action.payload.data));

      // read all included into the entity map
      if (action.payload && 'included' in action.payload && Array.isArray(action.payload.included))
        for (let i = 0; i < action.payload.included.length; i++)
          addEntityByType(entitiesByType, action.payload.included[i].type, deserialize(action.payload.included[i]));

      // for all entities types, in HIERARCHICAL ORDER from parents to children, update ORM store
      createOrUpdateAll(entitiesByType, "users", sess.User);
      createOrUpdateAll(entitiesByType, "accounts", sess.Account);
      createOrUpdateAll(entitiesByType, "account-users", sess.AccountUser);
      createOrUpdateAll(entitiesByType, "libraries", sess.Library);
      createOrUpdateAll(entitiesByType, "chains", sess.Chain);
      createOrUpdateAll(entitiesByType, "chain-bindings", sess.ChainBinding);
      createOrUpdateAll(entitiesByType, "chain-configs", sess.ChainConfig);
      createOrUpdateAll(entitiesByType, "instruments", sess.Instrument);
      createOrUpdateAll(entitiesByType, "instrument-memes", sess.InstrumentMeme);
      createOrUpdateAll(entitiesByType, "instrument-audios", sess.InstrumentAudio);
      createOrUpdateAll(entitiesByType, "instrument-audio-chords", sess.InstrumentAudioChord);
      createOrUpdateAll(entitiesByType, "instrument-audio-events", sess.InstrumentAudioEvent);
      createOrUpdateAll(entitiesByType, "programs", sess.Program);
      createOrUpdateAll(entitiesByType, "program-memes", sess.ProgramMeme);
      createOrUpdateAll(entitiesByType, "program-voices", sess.ProgramVoice);
      createOrUpdateAll(entitiesByType, "program-voice-tracks", sess.ProgramVoiceTrack);
      createOrUpdateAll(entitiesByType, "program-sequences", sess.ProgramSequence);
      createOrUpdateAll(entitiesByType, "program-sequence-bindings", sess.ProgramSequenceBinding);
      createOrUpdateAll(entitiesByType, "program-sequence-binding-memes", sess.ProgramSequenceBindingMeme);
      createOrUpdateAll(entitiesByType, "program-sequence-chords", sess.ProgramSequenceChord);
      createOrUpdateAll(entitiesByType, "program-sequence-patterns", sess.ProgramSequencePattern);
      createOrUpdateAll(entitiesByType, "program-sequence-pattern-events", sess.ProgramSequencePatternEvent);
      createOrUpdateAll(entitiesByType, "segments", sess.Segment);
      createOrUpdateAll(entitiesByType, "segment-messages", sess.SegmentMessage);
      createOrUpdateAll(entitiesByType, "segment-memes", sess.SegmentMeme);
      createOrUpdateAll(entitiesByType, "segment-chords", sess.SegmentChord);
      createOrUpdateAll(entitiesByType, "segment-choices", sess.SegmentChoice);
      createOrUpdateAll(entitiesByType, "segment-choice-arrangements", sess.SegmentChoiceArrangement);
      createOrUpdateAll(entitiesByType, "segment-choice-arrangement-picks", sess.SegmentChoiceArrangementPick);
      createOrUpdateAll(entitiesByType, "platform-messages", sess.PlatformMessage);
      createOrUpdateAll(entitiesByType, "works", sess.Work);

      // the state property of Session always points to the current database.
      // Updates don't mutate the original state, so this reference is not
      // equal to `dbState` that was an argument to this reducer.
      return sess.state;

    case API_ORM_DELETED:
      cleanup(modelFor(sess, action.entity), action.id);
      return sess.state;

    default:
      return state;
  }
};

// public
export default apiOrmReducer;
