//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Component from '@ember/component';
import {all, task} from 'ember-concurrency';
import {get, set} from '@ember/object';
import {htmlSafe} from '@ember/string';
import {inject as service} from '@ember/service';
import {Promise as EmberPromise} from 'rsvp';
import InstrumentPlayer from '../custom-objects/instrument-player';

// Configurations
const PROMPT_UPDATE_VELOCITY = 'Velocity';
const VELOCITY_TOGGLE_STEPS = [0.66, 0.33, 0.1, 0.05];
const DOUBLE_CLICK_MILLIS = 382;
const TASK_MAX_CONCURRENCY = 3;
const EVENT_DEFAULT_TONALITY = 0.618;
const EVENT_DEFAULT_NOTE = 'X';
const SWING_VALUE_MAX = 99;
const SWING_VALUE_MIN = 0;
const PLAY_CYCLE_SECONDS = 0.0618;
const PLAY_AHEAD_SECONDS = 1;

// States
const Initializing = 'Initializing';
const Standby = 'Standby';
const Working = 'Working';
const Playing = 'Playing';
const Prepping = 'Prepping';

/**
 [#159669804] Artist wants a step sequencer in order to compose rhythm patterns in a familiar way.
 */
const PatternStepmaticComponent = Component.extend(
  {
    // Name
    name: 'Stepmatic™',

    // State
    state: Initializing,

    // persist an InstrumentPlayer instance
    instrumentPlayer: null,

    // base URL for audio waveform files
    audioBaseUrl: '',

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

    // Is now playing?
    isPlaying: false,

    // is now preparing to play?
    isPrepping: false,

    // Is dirty? Any properties or grid steps changed.
    isDirty: false,

    // Current work status
    workStatus: 'Working...',

    // Play begin at seconds (with sub-millisecond floating point precision
    playBeginSeconds: 0,

    // Interval to persist play cycle
    playCycleInterval: {},

    // Time in seconds per step during preview play (based on sequence and pattern)
    playStepSeconds: 1,

    // The last step completed (as in scheduled for playback) during preview play
    playStepCompleted: -1,

    // The last beat to be marked as now-playing during preview play
    playBeatNow: -1,

    // select preview instrument from drop-down of all available instruments
    instruments: [],

    // selected instrument for preview play
    instrumentToPlay: null,

    // now-playing active beat lit up at the top of the grid during play
    gridMeter: [],

    // preparedness % of InstrumentPlayer loading audio
    preparednessPercentage: 0,

    // preparedness style width=n% string
    preparednessStyleWidth: htmlSafe('width: 0%;'),

    // queue of events to destroy when we apply stepmatic changes
    eventsToDestroy: [],

    /**
     * Initialize the component
     */
    init() {
      let self = this;
      this.config.getConfig().then(
        (config) => {
          self.set('audioBaseUrl', config.audioBaseUrl);
          self.initSequence();
        },
        (error) => {
          self.error('failed to load config', error);
        }
      );
      this._super(...arguments);
    },

    /**
     * Before component destruction
     */
    willDestroyElement() {
      let self = this;
      this.haltPlay().then(() => {
        self.debug('was halted and destroyed.');
        self._super(...arguments);
      }, () => {
        self.debug('was destroyed.');
        self._super(...arguments);
      })
    },

    /**
     * Go to the specified state.
     * Calls hooks depending on what state we are moving from/to
     * @param {String} targetState for transition into
     */
    goToState: function (targetState) {
      set(this, 'state', targetState);
      switch (targetState) {
        case Standby:
          set(this, 'isWorking', false);
          set(this, 'isPlaying', false);
          set(this, 'isPrepping', false);
          break;
        case Working:
          set(this, 'isPlaying', false);
          set(this, 'isPrepping', false);
          set(this, 'isWorking', true);
          break;
        case Prepping:
          set(this, 'isWorking', false);
          this.onPreppingProgress(0);
          set(this, 'isPlaying', true);
          set(this, 'isPrepping', true);
          break;
        case Playing:
          set(this, 'isWorking', false);
          set(this, 'isPrepping', false);
          set(this, 'isPlaying', true);
          break;
      }
    },

    /**
     * Initialize the component sequence
     */
    initSequence() {
      let self = this;
      let pattern = get(self, 'pattern');
      pattern.get('program').then(
        (sequence) => {
          self.debug('did load sequence', sequence);
          set(self, 'program', sequence);
          self.initInstruments();
        }, (error) => {
          get(self, 'display').error(error);
        }
      );
    },

    /**
     * Initialize all available instruments for preview play
     */
    initInstruments() {
      let self = this;
      let sequence = get(self, 'program');
      get(self, 'store').query('instrument', {libraryId: sequence.get('library').get('id')})
        .catch((error) => {
          get(self, 'display').error(error);
        })
        .then((instruments) => {
            let filteredInstruments = [];
            instruments.forEach((instrument) => {
              if ('Percussive' === instrument.get('type')) {
                filteredInstruments.push(instrument);
              }
            });
            self.debug('did load and filter instruments', filteredInstruments);
            set(self, 'instruments', filteredInstruments);
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
      let sequence = get(self, 'program');
      get(self, 'store').query('voice', {sequenceId: sequence.get('id')})
        .catch((error) => {
          get(self, 'display').error(error);
        })
        .then(voices => {
          self.debug('did load voices', voices);
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
          self.debug('did load events', events);
          set(self, 'events', events);
          self.createGridFromPatternEvents();
          self.goToState(Standby);
        });
    },

    /**
     * Compute the grid based on pattern events
     */
    createGridFromPatternEvents() {
      let pattern = this.pattern;
      let voices = this.voices;
      let total = pattern.get('total');
      let meterSuper = pattern.get('meterSuper');
      let meterSub = pattern.get('meterSub');
      let events = pattern.get('events');

      // clear grid
      set(this, 'grid', {});
      set(this, 'eventsToDestroy', []);
      set(this.meter, 'super', meterSuper);
      set(this.meter, 'sub', meterSub);
      let grid = this.grid;

      // clear gridMeter
      set(this, 'gridMeter', []);
      let gridMeter = this.gridMeter;
      for (let step = 0; step < meterSub * total; step++) {
        gridMeter.push(Math.floor(step / meterSub));
      }

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
          let trackName = event.get('name');
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
     * Re-compute the position and duration of all events
     */
    updateEventPositionDuration() {
      let pattern = this.pattern;
      let meterSub = pattern.get('meterSub');
      let meterSwing = pattern.get('meterSwing');

      // update all event positions
      this.forEachGridStep((groupId, trackName, step, event) => {
        if (event) { // else skip (no event at this step)
          let swing = isEven(step) ? 0 : meterSwing / (meterSub * 100);
          let position = step / meterSub + swing;
          if (position !== event.get('position')) {
            event.set('position', position);
          }
        }
      });

      // [#161041289]
      // Artist wants Stepmatic-generated events to have duration
      // lasting until the next event in the same voice, and no shorter.
      let groupStepDuration = this.updateGroupStepDuration();
      this.forEachGridStep((groupId, trackName, step, event) => {
        if (event) { // else skip (no event at this step)
          let duration = groupStepDuration[groupId][step];
          if (duration !== event.get('duration')) {
            event.set('duration', duration);
          }
        }
      });
    },

    /**
     * Compute the duration for an event in any given group at any given step.
     *
     * See: [#161041289] Artist wants Stepmatic-generated events to have duration
     *                   lasting until the next event in the same voice, and no shorter.
     *
     * @return {String}[]{String}[]{Number} array of [Group,Step] -> Duration
     */
    updateGroupStepDuration() {
      let pattern = this.pattern;
      let meterSub = pattern.get('meterSub');
      let total = pattern.get('total');

      // output data
      let duration = {};

      // for each group in grid
      let grid = this.grid;
      for (let groupId in grid) {
        if (grid.hasOwnProperty(groupId)) {
          duration[groupId] = {};
          let position = total;
          let priorPosition = total;
          let gridGroup = grid[groupId];
          let tracks = get(gridGroup, 'tracks');

          // for each step in track
          for (let step = meterSub * total - 1; step >= 0; step--) {

            // for each track in group
            for (let trackName in tracks) {
              if (tracks.hasOwnProperty(trackName)) {
                let track = tracks[trackName];
                let event = get(track, step);
                if (event) { // else skip (no event at step)
                  position = event.get('position');
                }
              }
            }

            // persist results of scanning the tracks in this group (at this step)
            duration[groupId][step] = priorPosition - position;
            priorPosition = position;
          }
        }
      }

      return duration;
    },

    /**
     * Callback on update to InstrumentPlayer load progress
     * @param {Number} preparedness from 0 to 1
     */
    onPreppingProgress(preparedness) {
      let percent = 5 + Math.floor(preparedness * 95);
      set(this, 'preparednessPercentage', percent);
      set(this, 'preparednessStyleWidth', htmlSafe('width: ' + percent + '%;'))
    },

    /**
     *
     [#154945824] Artist wants to load instrument audio for preview play of events
     */
    prepareForPlay() {
      this.goToState(Prepping);
      let sequence = this.program;
      let tempo = sequence.get('tempo');
      let pattern = this.pattern;
      let meterSub = pattern.get('meterSub');
      let instrument = this.instrumentToPlay;
      let instrumentPlayer = InstrumentPlayer.create({audioBaseUrl: this.audioBaseUrl});
      set(this, 'instrumentPlayer', instrumentPlayer);
      set(this, 'playStepSeconds', ((SECONDS_PER_MINUTE / tempo) / meterSub));
      set(this, 'playStepCompleted', -1);
      set(this, 'playBeatCompleted', -1);
      let self = this;
      get(self, 'store').query('audio', {instrumentId: instrument.get('id')})
        .then((audios) => {
          get(self, 'store').query('audio-event', {instrumentId: instrument.get('id')})
            .then((audioEvents) => {
              instrumentPlayer.setup(instrument, audios, audioEvents, (progress) => {
                self.onPreppingProgress(progress);
              }).then(() => {
                get(self, 'display').success('Loaded all audio for preview instrument ' + instrument.get('name') + '.');
                self.beginPlay();
              }, (error) => {
                self.displayErrorAndGoToReadyState(error);
              });
            }, (error) => {
              self.displayErrorAndGoToReadyState(error);
            });
        }, (error) => {
          self.displayErrorAndGoToReadyState(error);
        });
    },

    /**
     * [#160273003] Artist wants to press Play in order to begin play current pattern
     */
    beginPlay() {
      this.goToState(Playing);
      let instrumentPlayer = this.instrumentPlayer;
      let secondsPerStep = this.playStepSeconds;
      let pattern = this.pattern;
      let meterSub = pattern.get('meterSub');
      let total = pattern.get('total');
      set(this, 'playBeginSeconds', this.nowSeconds() + PLAY_AHEAD_SECONDS);
      set(this, 'playerOffsetSeconds', instrumentPlayer.currentTime());

      let self = this;
      set(this, 'playCycleInterval', setInterval(() => {
        self.doPlayCycle(secondsPerStep, meterSub, total);
      }, Math.round(PLAY_CYCLE_SECONDS * MILLIS_PER_SECOND)));
    },

    /**
     * [#160273003] Artist wants to press Stop in order to halt play of current pattern
     * @return {EmberPromise} promise resolved after playback has been halted
     */
    haltPlay() {
      clearInterval(this.playCycleInterval);
      this.clearPlayBeat();
      return new EmberPromise((resolve, reject) => {
        this.instrumentPlayer.stop().then(() => {
          this.goToState(Standby);
          resolve();
        }, reject);
      });
    },

    /**
     * Get now play seconds since begin play, in sub-millisecond floating point precision
     * @returns {Number}
     */
    playNowSeconds() {
      return this.nowSeconds() - this.playBeginSeconds;
    },

    /**
     * [#160273003] Artist wants play in a perfect loop, updating in real time with the step grid.
     * Passed in cached values from original function that instantiated the play cycle:
     * This process sets up audio for all steps that have not been scheduled

     *
     * @param secondsPerStep seconds per step
     * @param meterSub steps per beat
     * @param total beats
     */
    doPlayCycle(secondsPerStep, meterSub, total) {
      let grid = this.grid;
      let stepPrior = this.playStepCompleted;
      let nowSeconds = this.playNowSeconds();
      let playerOffsetSeconds = this.playerOffsetSeconds;
      let stepsPerMeasure = total * meterSub;
      let secondsPerBeat = meterSub * secondsPerStep;
      let secondsPerMeasure = secondsPerBeat * total;
      let instrumentPlayer = this.instrumentPlayer;

      this.doPlayBeatUpdate(Math.floor(nowSeconds / secondsPerStep), meterSub, total);

      let stepNext = Math.floor((nowSeconds + PLAY_AHEAD_SECONDS) / secondsPerStep);
      if (stepNext > stepPrior) {
        for (let stepNow = stepPrior + 1; stepNow <= stepNext; stepNow++) {
          let step = stepNow % stepsPerMeasure;
          let measure = Math.floor(stepNow / stepsPerMeasure);
          for (let groupId in grid) {
            if (grid.hasOwnProperty(groupId)) {
              let gridGroup = grid[groupId];
              let tracks = get(gridGroup, 'tracks');
              for (let trackName in tracks) {
                if (tracks.hasOwnProperty(trackName)) {
                  let track = tracks[trackName];
                  let event = get(track, step);
                  if (event) { // else skip (no event at step)
                    let playAudioAt = playerOffsetSeconds + PLAY_AHEAD_SECONDS + measure * secondsPerMeasure + event.get('position') * secondsPerBeat - numberOrZero(event.get('start'));
                    let playAudioDuration = event.get('duration') * secondsPerBeat;
                    instrumentPlayer.scheduleAudio(event.get('name'), event.get('velocity'), playAudioAt, playAudioDuration);
                  }
                }
              }
            }
          }
        }
        set(this, 'playStepCompleted', stepNext);
      }
    },

    /**
     * [#160273003] now-playing active beat lit up at the top of the grid during playback
     *
     * Passed in cached values from original function that instantiated the play cycle:
     * @param step currently on
     * @param meterSub steps per beat
     * @param total beats
     */
    doPlayBeatUpdate(step, meterSub, total) {
      let playBeatNow = this.playBeatNow;
      let beat = Math.floor(step / meterSub) % total;
      if (beat !== playBeatNow) {
        set(this, 'playBeatNow', beat);
        for (let i = 0; i < total; i++) {
          if (step >= 0 && beat === i) {
            // ('.beat-' + i)['addClass']('active'); FUTURE beat indicator
          } else {
            // ('.beat-' + i)['removeClass']('active'); FUTURE beat indicator
          }
        }
      }
    },

    /**
     * [#160273003] now-playing active beat no longer lit up after stop play
     */
    clearPlayBeat() {
      // ('.beat-step')['removeClass']('active'); FUTURE beat indicator
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
          self.debug('no handler for', self.clickCache[key].count, 'clicks', key);
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
     * modify event while playing and update in real time
     * fix Attempted to set 'velocity' to '1' on the deleted record <pattern-event:588>"
     * fix Attempted to handle event `deleteRecord` on <pattern-event:1662> while in state root.deleted.saved." @see if(this.isDestroyed)
     *
     * @param groupId to modify
     * @param trackName to modify
     * @param step to modify
     * @param velocity to update/create event with
     */
    modEventVelocity(groupId, trackName, step, velocity) {
      let groupContainer = get(this.grid, groupId);
      let tracks = get(groupContainer, 'tracks');
      let group = get(groupContainer, 'group');
      let track = get(tracks, trackName);
      let event = get(track, step);
      if (event) {
        set(event, 'velocity', velocity);
        if (0 === velocity) {
          this.eventsToDestroy.push(event);
          set(track, step, null);
        }
      } else {
        set(track, step, this.newEvent(group, trackName, velocity));
      }
      this.updateEventPositionDuration();
      this.gotDirty();
    },

    /**
     * New pattern event in voice
     * @param voice to create event event in
     * @param name of new event
     * @param velocity of new event
     * @returns {*|DS.Model|EmberPromise}
     */
    newEvent(voice, name, velocity) {
      let pattern = this.pattern;
      return this.store.createRecord('pattern-event', {
        pattern: pattern,
        voice: voice,
        velocity: velocity,
        name: name,
        duration: 1,
        tonality: EVENT_DEFAULT_TONALITY,
        note: EVENT_DEFAULT_NOTE,
      });
    },

    /**
     * Create an empty drum track
     * @param groupId to create track in
     * @param trackName of new track
     */
    createTrack(groupId, trackName) {
      let pattern = this.pattern;
      let total = pattern.get('total');
      let meterSub = pattern.get('meterSub');
      let groupContainer = get(this.grid, groupId);
      let tracks = get(groupContainer, 'tracks');
      set(tracks, trackName, {});
      for (let step = 0; step < meterSub * total; step++) {
        set(get(tracks, trackName), step, null);
      }
      this.display.success("Created track '" + trackName + "'");
    },

    /**
     * Apply stepmatic changes and all modifications to events
     * indicate updating state, until task group is complete
     */
    applyAll() {
      let self = this;
      self.goToState(Working);
      this.pattern.save().then(
        () => {
          self.eventUpdateAllTask.perform().then(() => {
            self.goToState(Standby);
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
    revertAll() {
      this.pattern.rollbackAttributes();
      this.forEachGridStep((groupId, trackName, step, event) => {
        if (event) {
          event.rollbackAttributes();
        }
      });
      this.createGridFromPatternEvents();
      this.gotClean();
      this.display.success('Reverted all changes.');
    },

    /**
     * Clear all grid steps by setting existing event velocity to zero
     */
    clearAll() {
      let self = this;
      self.forEachGridStep((groupId, trackName, step, event) => {
        if (event) {
          self.modEventVelocity(groupId, trackName, step, 0);
        }
      });
      this.display.success('Cleared all grid steps.');
    },

    /**
     * Apply stepmatic changes
     * Sends a cascade of HTTP requests to Update/Delete/Create related pattern events
     */
    eventUpdateAllTask: task(function* () {
      let self = this;
      let pattern = this.pattern;
      let eventsToDestroy = this.eventsToDestroy;
      let tasks = [];

      // send all destroy tasks
      eventsToDestroy.forEach((event) => {
        tasks.push(self.eventDestroyTask.perform(event));
      });

      // send all save tasks
      self.forEachGridStep((groupId, trackName, step, event) => {
        if (event) { // else skip (no event at this step)
          tasks.push(self.eventSaveTask.perform(event));
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
    forEachGridStep(callback) {
      let pattern = this.pattern;
      let meterSub = pattern.get('meterSub');
      let total = pattern.get('total');

      // for each group in grid
      let grid = this.grid;
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
    gotDirty() {
      set(this, 'isDirty', true);
    },

    /**
     * The component got clean, as in,
     * the pattern modifications were applied or reverted
     */
    gotClean() {
      set(this, 'isDirty', false);
    },

    /**
     * Now seconds with sub-millisecond floating point precision
     * @returns {number} of seconds since page navigation began
     */
    nowSeconds() {
      return window.performance.now() / MILLIS_PER_SECOND;
    },

    /**
     * Display an error, then go to ready state
     * @param error to display
     */
    displayErrorAndGoToReadyState(error) {
      if ('undefined' !== typeof error && error) {
        this.display.error(error);
      }
      try {
        this.goToState(Standby);
      } catch (e) {
        this.debug('cannot transition to standby', e);
      }
    },

    /**
     log a debug-level message
     * @param message
     * @param args
     */
    debug(message, ...args) {
      this.log('debug', message, ...args);
    },

    /**
     log a info-level message
     * @param message
     * @param args
     */
    info(message, ...args) {
      this.log('info', message, ...args);
    },

    /**
     log a warn-level message
     * @param message
     * @param args
     */
    warn(message, ...args) {
      this.log('warn', message, ...args);
    },

    /**
     log an error-level message
     * @param message
     * @param args
     */
    error(message, ...args) {
      this.log('error', message, ...args);
    },

    /**
     log any level of message
     * @param message
     * @param level
     * @param args
     */
    log(level, message, ...args) {
      console[level]('[' + this.name + '] ' + message, ...args);
    },

    // Component actions
    actions: {

      /**
       * [#160273003] Artist wants to press Play in order to begin play current pattern
       */
      play() {
        this.prepareForPlay();
      },

      /**
       * [#160273003] Artist wants to press Stop in order to halt play of current pattern
       */
      stop() {
        this.haltPlay();
      },

      /**
       * [#154945824] Artist wants to select instrument for preview play
       */
      setInstrumentToPlay(instrument) {
        set(this, 'instrumentToPlay', instrument);
      },

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
        let groupContainer = get(this.grid, groupId);
        let group = get(groupContainer, 'group');
        let groupName = group.get('name');
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
          this.createGridFromPatternEvents();
        }
      },

      /**
       * Update the value for # of divisions per beat,
       * and recompute the grid.
       */
      didUpdateSub() {
        if (confirm('Changing the subdivisions will revert all changes. Are you sure?')) {
          this.createGridFromPatternEvents();
        }
      },

      /**
       * Update the value for % delay odd steps.
       */
      didUpdateSwing(ev) {
        ev.target.value = limitSwingValue(ev.target.value);
        this.gotDirty();
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
 * Numeric value, or zero, based on input
 * @param value to filter
 * @returns {number} numeric value or zero
 */
function numberOrZero(value) {
  return typeof value !== 'undefined' && value && value > 0 ? value : 0;
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

// Obvious constants
const MILLIS_PER_SECOND = 1000;
const SECONDS_PER_MINUTE = 60;

// Finally, export the component
export default PatternStepmaticComponent;
