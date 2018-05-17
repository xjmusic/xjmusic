// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import {SegmentAudio} from "segment-audio";
import {API} from "api";

/**
 State constants
 * @type {string}
 */
const STANDBY = 'Standby';
const PLAYING = 'Playing';

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
 * @type {number}
 */
const CYCLE_RATIO_SUB_TO_MAIN = 10;

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
  name = 'XJ Music™ Player';

  /**
   * @type {boolean} whether in debug mode, which writes HTML output
   */
  isDebugMode = false;

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
  audioContext = Player.newAudioContext();

  /**
   * @type {number} millis UTC that audio context was initiated at.
   */
  audioContextStartMillisUTC = Date.now();

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
   * @type {number} count sub cycles between main cycles
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

    if (Player.contains(options, 'startAtMillisUTC')) {
      self.audioContextStartMillisUTC = options['startAtMillisUTC'];
      self.info('Playback will begin from specified millis UTC', self.audioContextStartMillisUTC);
    }

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
        self.state = PLAYING;
        self.api.config((config) => {
          self.segmentBaseUrl = config.segmentBaseUrl;
          self.startCycle();
        });
        self.info('Now playing', '#' + chain.embedKey, '(' + chain.name + ')');
      });
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
        self.state = STANDBY;
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
    if (Player.nonNull(this.chain.id)) {
      this.refreshDataThenUpdate();
    }
  }

  /**
   do Sub Cycle every N seconds
   */
  doSubCycle() {
    if (Player.nonNull(this.chain.id)) {
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
    return this.audioContext.currentTime;
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
    self.api.config((config) => {
      segments.forEach(segment => {
        self.updateSegmentAudio(config.segmentBaseUrl, segment);
        activeSegmentIds.push(segment.id);
        segmentsHtml += '<li>' + segment.state + '@<strong>' + segment.offset + '</strong>: ' + segment.waveformKey + '</li>';
      });
      self.teardownSegmentAudioExcept(activeSegmentIds);
      $('#chain-name').html(self.chain.name);
      $('#now-utc').html(new Date().toISOString());
      if (self.isDebugMode) {
        $('#segments').html(segmentsHtml);
      }
    });
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
    if (!segmentAudio) {
      segmentAudio = new SegmentAudio(self.audioContext, self.audioContextStartMillisUTC, segment, segmentBaseUrl);
      self.segmentAudios.set(segmentId, segmentAudio);
    }
    if (segmentAudio.isPlaying() && self.isDebugMode) {
      let URL = segmentBaseUrl + segment.waveformKey;
      $('#now-playing').html('<a href="' + URL + '" target="_blank">' + URL + '</a>');
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
        console.log("[player] tore down de-referenced segment id:" + segmentAudio.segment.offset);
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
   compute begin time of segment waveform in WebAudio context, relative to playFromTime
   * @param segment
   * @returns {*}
   */
  computeBeginTimeRelative(segment) {
    return Date.parse(segment.beginAt) / MILLIS_PER_SECOND - this.playFromMillisUTC;
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
   * Info to console
   * @param msg
   * @param optionalParams
   */
  info(msg, ...optionalParams) {
    console.info('[' + this.name + '] ' + msg, ...optionalParams);
  }

  /**
   * Debug to console
   * @param msg
   * @param optionalParams
   */
  debug(msg, ...optionalParams) {
    console.debug('[' + this.name + '] ' + msg, ...optionalParams);
  }

  /**
   * Error to console
   * @param msg
   * @param optionalParams
   */
  error(msg, ...optionalParams) {
    console.error('[' + this.name + '] ' + msg, ...optionalParams);
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
   Initialize WebAudio context
   <p>
   See API: https://developer.mozilla.org/en-US/docs/Web/API/AudioContext
   <p>
   Also see tutorial which includes reasoning for using the more complex constructor of the window.* context below: https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API
   */
  static newAudioContext() {
    if (window.AudioContext) {
      return new window.AudioContext();
    } else {
      return new window.webkitAudioContext();
    }
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

}

