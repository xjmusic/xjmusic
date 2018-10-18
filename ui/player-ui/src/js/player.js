// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import {SegmentAudio} from "segment-audio";
import {API} from "api";

/**
 State constants
 * @type {string}
 */
const STANDBY = 'Standby';
const SUSPEND = 'Suspend';
const SYNCING = 'Syncing';
const PLAYING = 'Playing';
const ALL_STATES = [STANDBY, SUSPEND, SYNCING, PLAYING];

/**
 * HTML icon codes
 * @type {string}
 */
const HTML_RIGHT_ARROW = '&#9658;';
const HTML_SPEAKER = '&#x1f50a;';
const HTML_SPACER = '&nbsp;&nbsp;';
const HTML_STATUS = {};
HTML_STATUS[PLAYING] = HTML_SPEAKER + HTML_SPACER + '<span class="hidden-when-small">Playing Live Stream</span>';
HTML_STATUS[SYNCING] = HTML_SPEAKER + HTML_SPACER + '<span class="hidden-when-small">Syncing...</span>';
HTML_STATUS[SUSPEND] = HTML_RIGHT_ARROW + HTML_SPACER + '<span class="hidden-when-small">Play</span>';
HTML_STATUS[STANDBY] = '<span class="hidden-when-small">Standby</span>';

/**
 Seconds to delay during a stop action
 note a stop-play cycle is 2x this
 * @type {number}
 */
const STOP_DELAY_SECONDS = 0.25;

/**
 Seconds between Main interval cycles
 * @type {number}
 */
const CYCLE_INTERVAL_SECONDS = 1;

/**
 Seconds between Sub interval cycles
 Adjusted in attempt to fix [#161303628] Listener expects to be able to listen to chain without listening itself causing problems for XJ
 @type {number}
 */
const CYCLE_RATIO_SUB_TO_MAIN = 60;

/**
 * Self-evident
 * @type {number}
 */
const MILLIS_PER_SECOND = 1000;

/**
 * [#150279546] Architect wants minimal, open-source web browser based XJ Music player, in order to embed XJ Music on any website, and ensure that the experience is as widely accessible as possible.
 */
export class Player {
  /**
   * @type {string} name of this module
   */
  name = 'XJ Music™';

  /**
   * @type {boolean} whether in debug mode, which writes HTML output
   */
  isDebugMode = false;

  /**
   * @type {boolean} whether to simulate blocked autoplay, for development purposes
   */
  simulateBlockedAutoplay = false;

  /**
   * @type {string} Chain identifier (embed key or id)
   */
  chainIdentifier = '';

  /**
   * @type {object} Chain currently playing
   */
  chain = null;

  /**
   * @type {string} State, Chain Segment
   */
  state = STANDBY;

  /**
   * @type {string} Base URL of segment waveforms
   */
  segmentBaseUrl = '';

  /**
   * @type {AudioContext} to store WebAudio context; which is never closed
   */
  audioContext = null;

  /**
   * @type {number} millis UTC that audio context was initiated at.
   */
  audioContextStartMillisUTC = 0;

  /**
   * one API instance per Player
   * @type {API}
   */
  api = new API();

  /**
   * @type {Array} all active segments
   */
  activeSegments = [];

  /**
   * @type {number} # seconds UTC; from which to play
   */
  playFromMillisUTC = 0;

  /**
   * @type {object} map of segment id to SegmentAudio
   */
  segmentAudios = new Map();

  /**
   * @type {object} interval to store Main cycle
   */
  cycleInterval = null;

  /**
   * @type {object} count sub cycles between main cycles
   */
  cycleSubTicker = null;

  /**
   * Instantiate a new Player
   * @param {Object} options
   */
  constructor(options) {
    let self = this;
    this.info('instantiated.');

    if (Player.contains(options, 'play')) {
      self.play(options['play']);
    }

    if (Player.contains(options, 'debug')) {
      self.isDebugMode = true;
    }

    if (Player.contains(options, 'autoplay')) {
      self.simulateBlockedAutoplay = true;
    }

    if (Player.contains(options, 'startAtMillisUTC')) {
      self.audioContextStartMillisUTC = options['startAtMillisUTC'];
      self.info('Playback will begin from specified millis UTC', self.audioContextStartMillisUTC);
    }

    self.updateStatus();
  }

  /**
   play a chain beginning at a certain segment
   start the interval cycles

   @param chainIdentifier to play
   */
  play(chainIdentifier) {
    let self = this;
    self.stop(() => {
      self.chainIdentifier = chainIdentifier;
      self.api.chain(self.chainIdentifier, (chain) => {
        self.chain = chain;
        self.info('Chain', '#' + chain.embedKey, '(' + chain.name + ')');
        self.suspend();
      });
    });
  }

  /**
   * Add a listener to resume playback when user interacted with the page.
   * [#150279553] Listener expects explicit *Play* button in player UI, for browsers that require explicit permission for audio playback.
   */
  suspend() {
    this.transitionToState(SUSPEND);
    let body = $('body');
    let self = this;
    body.on('click', function () {
      body.off();
      self.resume();
    });
  }

  /**
   * Resuming playback means creating an audio context while bubbling up from a user interaction
   * [#150279553] Listener expects explicit *Play* button in player UI, for browsers that require explicit permission for audio playback.
   */
  resume() {
    let self = this;
    self.createAudioContext();
    self.info('XJ Player did create WebAudio context on page interaction.');
    self.transitionToState(SYNCING);
    self.api.config((config) => {
      self.segmentBaseUrl = config.segmentBaseUrl;
      self.startCycle();
    });
  }

  /**
   Stop playback
   @param {function} thenFunc to execute after stopping
   */
  stop(thenFunc) {
    let self = this;
    if (Player.nonNull(self.chain)) {
      self.stopAllSegmentAudio(() => {
        self.chainIdentifier = '';
        self.chain = null;
        self.transitionToState(STANDBY);
        self.teardownSegmentAudioExcept([]);
        setTimeout(() => {
          thenFunc();
        }, STOP_DELAY_SECONDS * MILLIS_PER_SECOND);
      });
    } else {
      thenFunc();
    }
  }

  /**
   Do Main Cycle every N seconds
   */
  doMainCycle() {
    if (Player.nonNull(this.chain.id) && this.isActive()) {
      this.refreshDataThenUpdate();
    }
  }

  /**
   do Sub Cycle every N seconds
   */
  doSubCycle() {
    if (Player.nonNull(this.chain.id) && this.isActive()) {
      this.update();
    }
  }

  /**
   Check if there is a current chain, and if so, refresh it
   from seconds UTC must be integer for xj API here
   */
  refreshDataThenUpdate() {
    if (Player.nonNull(this.chain)) {
      let fromSecondsUTC = Math.floor((this.playFromMillisUTC / MILLIS_PER_SECOND) + this.currentTime());
      this.refreshCurrentChainDataThenUpdate(fromSecondsUTC);
    }
  }

  /**
   The player's current time, in seconds (float precision) since context start
   */
  currentTime() {
    return this.audioContext ? this.audioContext.currentTime : 0;
  }

  /**
   Load Chain Segments Data from seconds UTC, then update all segment audios
   * @param fromSecondsUTC
   */
  refreshCurrentChainDataThenUpdate(fromSecondsUTC) {
    let self = this;
    self.api.segments(self.chainIdentifier, (segments) => {
      self.activeSegments = segments;
      self.update();
    });
  }

  /**
   Update all segment audios
   */
  update() {
    let self = this;
    let segments = this.activeSegments;
    let activeSegmentIds = [];
    let segmentsHtml = '';
    if (null !== self.audioContext) {
      self.api.config((config) => {
        segments.forEach(segment => {
          self.updateSegmentAudio(config.segmentBaseUrl, segment);
          activeSegmentIds.push(segment.id);
          segmentsHtml += '<li>' + segment.state + '@<strong>' + segment.offset + '</strong>: ' + segment.waveformKey + '</li>';
        });
        self.teardownSegmentAudioExcept(activeSegmentIds);
        self.updateStatus();
        if (self.isDebugMode) {
          $('#chain-name').html(self.chain.name);
          $('#now-utc').html(new Date().toISOString());
          $('#segments').html(segmentsHtml);
        }
      });
    }
  }

  /**
   * Update status displayed in Player UI
   */
  updateStatus() {
    let body = $('body');
    let statusText = $('div#status-text');
    ALL_STATES.forEach((possibleState) => {
      let stateClass = 'state-' + possibleState.toLowerCase();
      if (possibleState === this.state) {
        body.addClass(stateClass);
      } else {
        body.removeClass(stateClass);
      }
    });
    statusText.html(HTML_STATUS[this.state]);
  }

  /**
   Update a segment audio
   @param segmentBaseUrl of segment waveforms
   @param segment
   */
  updateSegmentAudio(segmentBaseUrl, segment) {
    let self = this;
    let segmentId = segment.id;
    let segmentAudio = self.segmentAudios.get(segmentId);

    // Must be dubbed in order to instantiate segment audio
    if (segment.state.toLowerCase() === 'dubbed' && !segmentAudio) {
      segmentAudio = new SegmentAudio(self.audioContext, self.audioContextStartMillisUTC, segment, segmentBaseUrl);
      self.segmentAudios.set(segmentId, segmentAudio);
    }

    if (segmentAudio && segmentAudio.isPlaying()) {
      self.transitionToState(PLAYING);

      if (self.isDebugMode) {
        let URL = segmentBaseUrl + segment.waveformKey;
        $('#now-playing').html('<a href="' + URL + '" target="_blank">' + URL + '</a>');
      }
    }
  }

  /**
   garbage collection routine deletes any buffer source that is not in the current set of segment ids

   * @param activeSegmentIds
   */
  teardownSegmentAudioExcept(activeSegmentIds) {
    let self = this;
    this.segmentAudios.forEach((segmentAudio, segmentId) => {
      if (!Player.stringInArray(segmentId.toString(), activeSegmentIds)) {
        self.segmentAudios.delete(segmentId);
        self.info('tore down de-referenced [segment-' + segmentAudio.segment.offset + ']');
      }
    });
  }

  /**
   Stop all SegmentAudio buffer source playback via WebAudio API
   @param {function} thenFunc to execute after stopping all segment audio
   */
  stopAllSegmentAudio(thenFunc) {
    let self = this;
    try {
      self.segmentAudios.forEach((segmentAudio) => {
        segmentAudio.stopWebAudio();
      });
      thenFunc();
    } catch (e) {
      self.error('Failed to stop all segment audio!', e);
    }
  }

  /**
   start the main cycle interval, and sub cycle interval
   */
  startCycle() {
    let self = this;
    self.cycleSubTicker = 0;

    self.cycleInterval = setInterval(function () {
      self.cycleSubTicker++;
      if (self.cycleSubTicker >= CYCLE_RATIO_SUB_TO_MAIN) {
        self.cycleSubTicker = 0;
        self.doMainCycle();
      } else {
        self.doSubCycle();
      }
    }, CYCLE_INTERVAL_SECONDS * MILLIS_PER_SECOND);

    self.doMainCycle();
  }

  /**
   * Transition to new state, and update status
   * @param toState to transition to
   */
  transitionToState(toState) {
    this.state = toState;
    this.updateStatus();
  }

  /**
   log a debug-level message
   * @param message
   * @param args
   */
  debug(message, ...args) {
    this.log('debug', message, ...args);
  }

  /**
   log a info-level message
   * @param message
   * @param args
   */
  info(message, ...args) {
    this.log('info', message, ...args);
  }

  /**
   log a warn-level message
   * @param message
   * @param args
   */
  warn(message, ...args) {
    this.log('warn', message, ...args);
  }

  /**
   log an error-level message
   * @param message
   * @param args
   */
  error(message, ...args) {
    this.log('error', message, ...args);
  }

  /**
   log any level of message
   * @param message
   * @param level
   * @param args
   */
  log(level, message, ...args) {
    console[level]('[' + this.name + '] ' + message, ...args);
  }

  /**
   Initialize WebAudio context
   <p>
   See API: https://developer.mozilla.org/en-US/docs/Web/API/AudioContext
   <p>
   Also see tutorial which includes reasoning for using the more complex constructor of the window.* context below: https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API
   <p>
   Also dry-fire an empty sound in a further attempt to enable to WebAudio context on iOS devices
   [#159579536] Apple iOS user (on Chrome or Firefox mobile browser) expects to be able to hear XJ Music Player embedded a in web browser.
   */
  createAudioContext() {
    let self = this;
    if (window.AudioContext) {
      self.audioContext = new window.AudioContext();
    } else {
      self.audioContext = new window.webkitAudioContext();
    }
    if (0 === self.audioContextStartMillisUTC) {
      self.audioContextStartMillisUTC = Date.now();
    }

    // create empty buffer
    let buffer = self.audioContext.createBuffer(1, 1, 22050);
    let dryFire = self.audioContext.createBufferSource();
    dryFire.buffer = buffer;

    // connect to output (your speakers)
    dryFire.connect(self.audioContext.destination);

    // play the file
    dryFire.start(0);
  }

  /**
   Assume array of strings, find needle in haystack
   * @param {String} needle
   * @param {Array} haystack
   * @return boolean
   */
  static stringInArray(needle, haystack) {
    for (let i = 0; i < haystack.length; i++) {
      if (String(haystack[i]) === String(needle)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Does object contain key?
   * @param obj containing keys
   * @param key to check for
   * @returns {boolean} if object contains key
   */
  static contains(obj, key) {
    if ('undefined' === typeof obj) return false;
    if ('object' !== typeof obj) return false;
    return key in obj && obj.hasOwnProperty(key);
  }

  /**
   Whether the object is non-null
   * @param obj
   * @returns {boolean}
   */
  static nonNull(obj) {
    return !Player.isNull(obj);
  }

  /**
   Whether the object is null
   * @param obj
   * @returns {boolean}
   */
  static isNull(obj) {
    return obj === undefined || obj === null;
  }

  /**
   * Is the player active (NOT suspended)?
   * @returns {Boolean} true if the player is active
   */
  isActive() {
    return this.state === SYNCING || this.state === PLAYING;
  }

}

