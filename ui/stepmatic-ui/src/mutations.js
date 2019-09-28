const TYPES = [
  'events',
  'patterns',
  'program-memes',
  'sequence-binding-memes',
  'sequence-bindings',
  'sequence-chords',
  'sequences',
  'tracks',
  'voices',
];
const FLASH_MESSAGE_TIMEOUT_MILLIS = 5000;

/**
 * The state is now dirty
 *
 * @param state to mutate
 */
function nowDirty(state) {
  if (!state.dirty) {
    state.dirty = true;
    window.onbeforeunload = () => {
      return "Are you sure you want to navigate away?";
    };
    console.log("dirty!");
  }
}

/**
 * The state is now clean
 *
 * @param state to mutate
 */
function nowClean(state) {
  if (state.dirty) {
    state.dirty = false;
    window.onbeforeunload = null;
    console.log("clean!");
  }
}

/**
 * Destroy an entity (at the top level of the program) by entity type and id
 *
 * @param state to mutate
 * @param type (plural) of entity to destroy
 * @param id of entity to destroy
 */
function destroyEntity(state, type, id) {
  // remove this entity from all hasMany arrays
  TYPES.forEach(parentType => {
    if (type in state.program[parentType]) {
      let i = state.program[parentType][type].map(item => item.id).indexOf(id);
      if (i > -1)
        state.program[parentType][type].splice(i, 1);
    }
  });

  // remove entity from program top level
  let i = state.program[type].map(item => item.id).indexOf(id);
  if (i > -1)
    state.program[type].splice(i, 1);
}

/**
 * Destroy all child entities of a given type, with a given parent type and parent id
 *
 * @param state to mutate
 * @param type (plural) of entities to destroy
 * @param parentRelation (singular) of entity for which to destroy child entities
 * @param parentId of entity for which to destroy child entities
 */
function destroyChildEntities(state, type, parentRelation, parentId) {
  let ids = [];
  state.program[type].forEach(entity => {
    if (parentRelation in entity && parentId === entity[parentRelation].id)
      ids.push(entity.id);
  });
  ids.forEach(id => {
    destroyEntity(state, type, id);
  });
}

/*--------------------------------------------
|     Following are mutation definitions     |
--------------------------------------------*/
export default {

  /**
   * Set the active sequence
   *
   * @param state to mutate
   * @param sequence
   */
  setActiveSequence(state, sequence) {
    state.activeSequence = sequence;
  },

  /**
   * Set the active timeline grid
   *
   * @param state to mutate
   * @param grid
   */
  setActiveTimelineGrid(state, grid) {
    if (grid)
      state.activeTimelineGrid = grid;
    else
      state.activeTimelineGrid = state.timelineGrids[0];
  },

  /**
   * Update an entity in the Program, given the entity type, id, and a set of attribute-value pairs to update
   *
   * @param state to mutate
   * @param payload{type:{String}, id:{String}, attrs:{String:Object}}
   */
  updateEntity(state, payload) {
    for (let i = 0; i < state.program[payload.type].length; i++)
      if (state.program[payload.type][i].id === payload.id)
        for (let key in payload.attrs)
          if (payload.attrs.hasOwnProperty(key))
            state.program[payload.type][i][key] = payload.attrs[key];
    nowDirty(state);
  },

  /**
   * Destroy a pattern in the program, by id.
   *
   * @param state to mutate
   * @param patternId to destroy
   */
  destroyPattern(state, patternId) {
    destroyChildEntities(state, 'events', 'pattern', patternId);
    destroyEntity(state, 'patterns', patternId);
    nowDirty(state);
  },

  /**
   * Will begin saving
   *
   * @param state to mutate
   */
  willSave(state) {
    state.saving = true;
  },

  /**
   * Saved the program
   *
   * @param state to mutate
   * @param program data that was saved
   */
  didSave(state, program) {
    state.program = program;
    state.saving = false;
    nowClean(state);
  },

  /**
   * Failed to save
   *
   * @param state to mutate
   * @param error returned from API
   */
  failedToSave(state, error) {
    error.body.errors.forEach(errObj => {
      console.log("Failed to save", errObj);
      let msg = `Failed to save! ${errObj.code} "${errObj.title}"`;
      window.mainVue.flash(msg, 'error', {
        timeout: FLASH_MESSAGE_TIMEOUT_MILLIS,
      });
    });
    // alert(`${error.statusText}! (Code ${error.status})`);
    state.saving = false;
  }

};
