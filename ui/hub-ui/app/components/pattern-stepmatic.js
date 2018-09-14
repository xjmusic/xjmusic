//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import Component from '@ember/component';
import {get, set} from '@ember/object';
import {inject as service} from '@ember/service';
import {all, task} from 'ember-concurrency';
import $ from 'jquery';

const PROMPT_UPDATE_VELOCITY = 'Velocity';
const VELOCITY_TOGGLE_STEPS = [0.66, 0.33, 0.1, 0.05];
const DOUBLE_CLICK_MILLIS = 382;
const TASK_MAX_CONCURRENCY = 3;
const EVENT_DEFAULT_TONALITY = 0.618;
const EVENT_DEFAULT_NOTE = 'X';
const SWING_VALUE_MAX = 99;
const SWING_VALUE_MIN = 0;


/**
 [#159669804] Artist wants a step sequencer in order to compose rhythm patterns in a familiar way.
 */
const PatternStepmaticComponent = Component.extend(
  {
    // Inject: configuration service
    config: service(),

    // Inject: flash message service
    display: service(),

    // Inject: ember data store service
    store: service(),

    // Cache timeouts for detecting single vs. double click
    clickCache: {},

    // Sequence (parent of pattern being edited)
    sequence: {},

    // Voices in sequence
    voices: {},

    // Pattern being edited
    pattern: {},

    // Meter
    meter: {
      super: 4, // super (beats per measure)
      sub: 4 // sub (divisions per beat)
    },

    // Events in pattern
    events: {},

    // Grid for step sequencing
    grid: {},

    // Is now working?
    isWorking: false,

    // Is dirty? Any properties or grid steps changed.
    isDirty: false,

    // Current work status
    workStatus: 'Working...',

    /**
     * Initialize the component
     */
    init() {
      let self = this;
      get(self, 'config').promises.config.then(
        () => {
          self.initTooltips()
          self.initSequence();
        },
        (error) => {
          console.error('Failed to load config', error);
        }
      );
      this._super(...arguments);
    },

    /**
     * Initialize tooltips (Bootstrap)
     */
    initTooltips: function () {
      $(function () {
        $('[data-toggle="tooltip"]').tooltip();
      })
    },

    /**
     * Initialize the component sequence
     */
    initSequence() {
      let self = this;
      let pattern = get(self, 'pattern');
      pattern.get('sequence').then(
        (sequence) => {
          console.debug('[stepmatic] did load sequence', sequence);
          set(self, 'sequence', sequence);
          self.initVoices();
        }, (error) => {
          get(self, 'display').error(error);
        }
      );
    },

    /**
     * Initialize the component sequence
     */
    initVoices() {
      let self = this;
      let sequence = get(self, 'sequence');
      get(self, 'store').query('voice', {sequenceId: sequence.get('id')})
        .catch((error) => {
          get(self, 'display').error(error);
        })
        .then(voices => {
          console.debug('[stepmatic] did load voices', voices);
          set(self, 'voices', voices);
          self.initPattern();
        });
    },

    /**
     * Initialize the component pattern
     */
    initPattern() {
      let self = this;
      let pattern = get(self, 'pattern');
      get(self, 'store').query('pattern-event', {patternId: pattern.get('id')})
        .catch((error) => {
          get(self, 'display').error(error);
        })
        .then(events => {
          console.debug('[stepmatic] did load events', events);
          set(self, 'events', events);
          self.computeGrid();
        });
    },

    /**
     * Compute the grid based on pattern events
     */
    computeGrid() {
      let self = this;
      let pattern = get(this, 'pattern');
      let voices = get(this, 'voices');
      let total = pattern.get('total');
      let meterSuper = pattern.get('meterSuper');
      let meterSub = pattern.get('meterSub');
      let events = pattern.get('events');

      // clear grid
      set(self, 'grid', {});
      set(get(self, 'meter'), 'super', meterSuper);
      set(get(self, 'meter'), 'sub', meterSub);
      let grid = get(self, 'grid');

      // compute groups from voices
      voices.forEach(voice => {
        let groupId = voice.get('id');
        let gridGroup = {};
        set(gridGroup, 'group', voice);
        set(gridGroup, 'tracks', {});
        set(grid, groupId, gridGroup);
      });

      // compute grid steps from events
      events.forEach(event => {
        event.get('voice').then((group) => {
          let groupId = group.get('id');
          let tracks = get(get(grid, groupId), 'tracks');

          // if drum not yet seen in group, create empty grid
          let trackName = event.get('inflection');
          if (!has(tracks, trackName)) {
            set(tracks, trackName, {});
            for (let step = 0; step < meterSub * total; step++) {
              set(get(tracks, trackName), step, null);
            }
          }
          let track = get(tracks, trackName);

          // compute step and velocity
          let step = Math.round(meterSub * event.get('position'));

          // set the drumGroup-drum grid position to the event
          set(track, step, event);
        });
      });
    },

    /**
     * Do three different actions, based on single, double, or triple-click.
     *
     * @param key to identify a unique click
     * @param onSingleFn
     * @param onDoubleFn
     * @param onTripleFn
     */
    doSingleDoubleTriple(key, onSingleFn, onDoubleFn, onTripleFn) {
      let self = this;

      let onTimeout = () => { // to execute after any number of clicks
        if (1 === self.clickCache[key].count) {
          onSingleFn();
        } else if (2 === self.clickCache[key].count) {
          onDoubleFn();
        } else if (3 === self.clickCache[key].count) {
          onTripleFn();
        } else {
          console.debug('no handler for', self.clickCache[key].count, 'clicks', key);
        }
        delete self.clickCache[key];
      };

      if (has(self.clickCache, key)) { // there is already a click for this key, clear its original timeout and update it.
        clearTimeout(this.clickCache[key].timeout);
        this.clickCache[key].count++;
        self.clickCache[key].timeout = setTimeout(onTimeout, DOUBLE_CLICK_MILLIS);

      } else { // this is the first click
        self.clickCache[key] = {
          timeout: setTimeout(onTimeout, DOUBLE_CLICK_MILLIS),
          count: 1
        };
      }
    },

    /**
     * Modify event velocity at grid step; create event if none exists
     *
     * @param groupId to modify
     * @param trackName to modify
     * @param step to modify
     * @param velocity to update/create event with
     */
    modEventVelocity(groupId, trackName, step, velocity) {
      let groupContainer = get(get(this, 'grid'), groupId);
      let tracks = get(groupContainer, 'tracks');
      let group = get(groupContainer, 'group');
      let track = get(tracks, trackName);
      let event = get(track, step);
      if (event) {
        set(event, 'velocity', velocity);
      } else {
        set(track, step, this.newEvent(group, trackName, velocity));
      }
      this.gotDirty();
    },

    /**
     * New pattern event in voice
     * @param voice to create event event in
     * @param inflection of new event
     * @param velocity of new event
     * @returns {*|DS.Model|EmberPromise}
     */
    newEvent: function (voice, inflection, velocity) {
      let pattern = get(this, 'pattern');
      let meterSub = pattern.get('meterSub');
      return get(this, 'store').createRecord('pattern-event', {
        pattern: pattern,
        voice: voice,
        velocity: velocity,
        inflection: inflection,
        duration: 1 / meterSub,
        tonality: EVENT_DEFAULT_TONALITY,
        note: EVENT_DEFAULT_NOTE,
      });
    },

    /**
     * Create an empty drum track
     * @param groupId to create track in
     * @param trackName of new track
     */
    createTrack: function (groupId, trackName) {
      let pattern = get(this, 'pattern');
      let total = pattern.get('total');
      let meterSub = pattern.get('meterSub');
      let groupContainer = get(get(this, 'grid'), groupId);
      let tracks = get(groupContainer, 'tracks');
      set(tracks, trackName, {});
      for (let step = 0; step < meterSub * total; step++) {
        set(get(tracks, trackName), step, null);
      }
      get(this, 'display').success("Created track '" + trackName + "'");
    },

    /**
     * Apply stepmatic changes and all modifications to events
     * indicate updating state, until task group is complete
     */
    applyAll: function () {
      let self = this;
      set(self, 'isWorking', true);
      get(this, 'pattern').save().then(
        () => {
          self.eventUpdateAllTask.perform().then(() => {
            set(self, 'isWorking', false);
            self.gotClean();
          });
        },
        (error) => {
          get(self, 'display').error(error);
        });
    },

    /**
     * Revert all stepmatic changes to original pattern and pattern events
     */
    revertAll: function () {
      get(this, 'pattern').rollbackAttributes();
      this.forEachGridStep((groupId, trackName, step, event) => {
        if (event) {
          event.rollbackAttributes();
        }
      });
      this.computeGrid();
      this.gotClean();
      get(this, 'display').success('Reverted all changes.');
    },

    /**
     * Clear all grid steps by setting existing event velocity to zero
     */
    clearAll: function () {
      let self = this;
      self.forEachGridStep((groupId, trackName, step, event) => {
        if (event) {
          self.modEventVelocity(groupId, trackName, step, 0);
        }
      });
      get(this, 'display').success('Cleared all grid steps.');
    },

    /**
     * Apply stepmatic changes
     * Sends a cascade of HTTP requests to Update/Delete/Create related pattern events
     */
    eventUpdateAllTask: task(function* () {
      let self = this;
      let pattern = get(this, 'pattern');
      let meterSub = pattern.get('meterSub');
      let meterSwing = pattern.get('meterSwing');
      let tasks = [];

      self.forEachGridStep((groupId, trackName, step, event) => {
        // skip if null
        if (event) {
          let swing = isEven(step) ? 0 : meterSwing / (meterSub * 100);
          let position = step / meterSub + swing;
          if (position !== event.get('position')) {
            event.set('position', position);
          }

          // if event velocity is zero, delete
          if (0 === event.get('velocity')) {
            tasks.push(self.eventDestroyTask.perform(event));

            // otherwise, save it
          } else if (event.get('isNew') || event.get('hasDirtyAttributes')) {
            tasks.push(self.eventSaveTask.perform(event));
          }
        }
      });

      yield all(tasks);
      get(self, 'display').success('Applied stepmatic to pattern ' + pattern.get('name') + '.');
    }),

    /**
     * delete an event, as child to concurrent task group
     * @param event to delete
     */
    eventDestroyTask: task(function* (event) {
      yield event.destroyRecord({});
    }).enqueue().maxConcurrency(TASK_MAX_CONCURRENCY),

    /**
     * save an event, as child to concurrent task group
     * @param event to save
     */
    eventSaveTask: task(function* (event) {
      yield event.save();
    }).enqueue().maxConcurrency(TASK_MAX_CONCURRENCY),

    /**
     * Execute a function for each grid step
     * @param callback to execute
     */
    forEachGridStep: function (callback) {
      let pattern = get(this, 'pattern');
      let meterSub = pattern.get('meterSub');
      let total = pattern.get('total');

      // for each group in grid
      let grid = get(this, 'grid');
      for (let groupId in grid) {
        if (grid.hasOwnProperty(groupId)) {
          let gridGroup = grid[groupId];
          let tracks = get(gridGroup, 'tracks');

          // for each track in group
          for (let trackName in tracks) {
            if (tracks.hasOwnProperty(trackName)) {
              let track = tracks[trackName];

              // for each step in track
              for (let step = 0; step < meterSub * total; step++) {
                let event = get(track, step);

                // execute callback
                callback(groupId, trackName, step, event);
              }
            }
          }
        }
      }
    },

    /**
     * The component got dirty, as in,
     * modifications were made to pattern properties or the step grid
     */
    gotDirty: function () {
      set(this, 'isDirty', true);
    },

    /**
     * The component got clean, as in,
     * the pattern modifications were applied or reverted
     */
    gotClean: function () {
      set(this, 'isDirty', false);
    },

    // Component actions
    actions: {

      /**
       * Apply stepmatic changes
       */
      apply() {
        if (confirm('This will apply all changes, potentially modifying all events in this pattern. Are you sure?')) {
          this.applyAll();
        }
      },

      /**
       * Revert stepmatic
       * re-computes grid from original pattern events
       */
      revert() {
        if (confirm('This will revert all changes. Are you sure?')) {
          this.revertAll();
        }
      },

      /**
       * Clear all events in stepmatic
       * re-computes grid from original pattern events
       */
      clear() {
        if (confirm('This will clear all grid steps. Are you sure?')) {
          this.clearAll();
        }
      },

      /**
       * Add a track to a group in the stepmatic
       */
      addTrack(groupId) {
        let groupContainer = get(get(this, 'grid'), groupId);
        let group = get(groupContainer, 'group');
        let groupName = group.get('description');
        let name = prompt('Add track to ' + groupName + ' group:', 'New track');
        if (null !== name) {
          this.createTrack(groupId, name);
        }
      },

      /**
       * Update the value for # of beats per measure,
       * and recompute the grid.
       */
      didUpdateSuper() {
        if (confirm('Changing the beat superset will revert all changes. Are you sure?')) {
          this.computeGrid();
        }
      },

      /**
       * Update the value for # of divisions per beat,
       * and recompute the grid.
       */
      didUpdateSub() {
        if (confirm('Changing the subdivisions will revert all changes. Are you sure?')) {
          this.computeGrid();
        }
      },

      /**
       * Update the value for % delay odd steps.
       */
      didUpdateSwing(ev) {
        ev.target.value = limitSwingValue(ev.target.value);
      },

      /**
       * Touch the stepmatic grid to make a modification
       * @param groupId to modify
       * @param trackName to modify
       * @param step to modify
       */
      onStepTouch(groupId, trackName, step) {
        let self = this;
        let groupContainer = get(get(self, 'grid'), groupId);
        let tracks = get(groupContainer, 'tracks');
        let track = get(tracks, trackName);
        let priorVelocity = velocityOrZero(get(track, step));
        self.doSingleDoubleTriple(key3D(groupId, trackName, step),

          () => { // single-click to toggle velocity
            self.modEventVelocity(groupId, trackName, step, toggleVelocity(priorVelocity));
          },

          () => { // double-click to set to zero
            self.modEventVelocity(groupId, trackName, step, 0);
          },

          () => { // triple-click to set exact value
            let updatedVelocity = promptVelocity(priorVelocity);
            if (null !== updatedVelocity) {
              self.modEventVelocity(groupId, trackName, step, updatedVelocity);
            }
          });
      },

    }
  });

/**
 * Return velocity value (including zero) or zero
 * @param {Ember.Object} obj
 * @returns {number}
 */
function velocityOrZero(obj) {
  if (obj) {
    let velocity = get(obj, 'velocity');
    return 0 < velocity ? velocity : 0;
  } else {
    return 0;
  }
}


/**
 * Object has property?
 */
function has(obj, prop) {
  return prop in obj;
}

/**
 * Toggle velocity on click
 */
function toggleVelocity(v) {
  if (0 === v) {
    return 1;
  }
  for (const s of VELOCITY_TOGGLE_STEPS) {
    if (s < v) {
      return s;
    }
  }
  return 0;
}

/**
 * Limit a value provided for Swing %
 * @param value to limit
 * @returns {number} limited value
 */
function limitSwingValue(value) {
  let limitedValue = Math.min(Math.max(SWING_VALUE_MIN, value), SWING_VALUE_MAX);
  return limitedValue > 0 ? limitedValue : 0;
}

/**
 * Detail velocity value
 * @param defaultValue
 * @returns {string | null}
 */
function promptVelocity(defaultValue) {
  return prompt(PROMPT_UPDATE_VELOCITY, defaultValue);
}

/**
 * Unique Key for a 3-dimensionally identified point
 * @param Nx location in dimension 1
 * @param Ny location in dimension 2
 * @param Nz location in dimension 3
 */
function key3D(Nx, Ny, Nz) {
  return String(Nx) + '___' + String(Ny) + '___' + String(Nz);
}

/**
 * Is integer odd?
 * @param num
 * @returns {boolean}
 */
function isEven(num) {
  return 0 === (num % 2);
}

/**
 * Usage (e.g, in Handlebars, where pattern model is "myPatternModel"):
 *
 *   {{pattern-stepmatic myPatternModel}}
 */
PatternStepmaticComponent.reopenClass(
  {
    positionalParams: ['pattern']
  });

export default PatternStepmaticComponent;
