// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import {DOM} from "dom";

/**
 * RegExp to test for a valid URL
 * @type {RegExp}
 */
const rgxValidUrl = /(http|https):\/\/(\w+:?\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@\-\/]))?/;

/**
 * # of seconds of silence to enforce from beginning (0.0 seconds) of WebAudio context
 */
const ENFORCE_WEBAUDIO_SCHEDULE_PLAYBACK_AHEAD_SECONDS = 0.5;

/**
 * self evident maths
 */
const MILLIS_PER_SECOND = 1000;


/**
 State constants
 * @type {string}
 */
const STANDBY = 'Standby';
const SYNCING = 'Syncing';
const PLAYING = 'Playing';

/**
 SegmentAudio object, a wrapper to play the audio for a single segment in a chain.

 Manages this segment audio's buffer and playback in the audio context

 */
export class SegmentAudio {

  /**
   * @type {Object} HTML5 Web Audio context
   */
  audioContext = null;

  /**
   * @type {String} segmentBaseUrl for segment OGG's
   */
  segmentBaseUrl = '';

  /**
   * @type {object|{beginAt,endAt,waveformKey,offset}}
   */
  segment = null;

  /**
   * To load audio into; for playback
   * @type {Object}
   */
  source = null;

  /**
   * Time (seconds, floating point) to begin playback in WebAudio context
   */
  beginAtTime;

  /**
   * Time (seconds, floating point) to begin playback in WebAudio context
   */
  endAtTime;

  /**
   * State of player
   * @type {string}
   */
  state = STANDBY;

  /**
   * Instantiate a new Segment Audio playback controller
   * @param {AudioContext} audioContext shared by all segments currently playing in this chain
   * @param {number} audioContextStartMillisUTC
   * @param {object} segment to play
   * @param {string} segmentBaseUrl
   * @param {boolean} isDebugMode
   */
  constructor(audioContext, audioContextStartMillisUTC, segment, segmentBaseUrl, isDebugMode) {
    this.audioContext = audioContext;
    this.segmentBaseUrl = segmentBaseUrl;
    this.segment = segment;
    this.isDebugMode = isDebugMode;
    let self = this;

    // compute time-related properties
    self.beginAtTime = (Date.parse(self.segment.beginAt) - audioContextStartMillisUTC) / MILLIS_PER_SECOND;
    self.endAtTime = (Date.parse(self.segment.endAt) - audioContextStartMillisUTC) / MILLIS_PER_SECOND;
    self.debug(`Parsed from startMillisUTC=${audioContextStartMillisUTC}, begin(${self.segment.beginAt})=${self.beginAtTime}, end(${self.segment.endAt})=${self.endAtTime}`)

    if (!self.hasValidWaveformUrl()) {
      self.error(`Invalid waveform URL: ${self.getWaveformUrl()}`);
      return;
    }

    if (self.isAudioContextRunning() && self.isFutureEnough()) {
      self.transitionToState(SYNCING);
      self.loadAudio(() => {
        self.playWebAudio();
      });
    } else {
      self.source = null;
      self.warn(`Skipped @ ${new Date(self.segment.beginAt).toISOString()}`);
    }
  }

  /**
   tend to each active segment audio buffer
   sound starts precisely via direct message sent to WebAudio
   */
  playWebAudio() {
    let self = this;
    self.debug(`Will play ${self.getWaveformUrl()} @ ${new Date(self.segment.beginAt).toISOString()} (T-${self.beginAtTime - self.getCurrentTime()}s)`);
    if (self.isFutureEnough()) {
      self.transitionToState(PLAYING);
      self.source.start(self.beginAtTime, 0);
      self.info(`${self.getWaveformUrl()} @ ${self.beginAtTime}`);
    } else {
      self.warn("Skipped playback of audio without sufficient lead time to in WebAudio context");
    }
  }

  /**
   Sound stops
   */
  stopWebAudio() {
    try {
      this.source.stop();
    } catch (e) {
      // noop
    }
  }

  /**
   load waveforms into WebAudio buffers

   @param onSuccess
   */
  loadAudio(onSuccess) {
    let self = this;
    self.source = self.audioContext.createBufferSource();
    let request = new XMLHttpRequest();
    request.open('GET', self.getWaveformUrl(), true);
    request.responseType = 'arraybuffer';
    request.onload = function () {
      let audioData = request.response;
      self.debug(`Fetched audio data from ${self.getWaveformUrl()}`);
      try {
        self.audioContext.decodeAudioData(audioData,
          (buffer) => {
            self.source.buffer = buffer;
            self.source.connect(self.audioContext.destination);
            self.debug('loaded buffer source');
            onSuccess();
          },
          (e) => {
            self.error("Problem decoding audio data", e);
          }
        );
      } catch (e) {
        self.error("Caught exception while decoding audio data", e);
      }
    }
    request.send();
  }

  /**
   has valid waveform url?
   */
  hasValidWaveformUrl() {
    return rgxValidUrl.test(this.getWaveformUrl());
  }

  /**
   * @return {string} url of waveform
   */
  getWaveformUrl() {
    return this.segmentBaseUrl + this.segment.waveformKey;
  }

  /**
   * [#150279553] Listener expects explicit **Play** button in player UI, for browsers that require explicit permission for audio playback.
   * @returns {boolean}
   */
  isAudioContextRunning() {
    return this.audioContext.state && this.audioContext.state.toLowerCase() === 'running';
  }

  /**
   * Transition to new state, and update status
   * @param toState to transition to
   */
  transitionToState(toState) {
    this.state = toState;
  }

  /**
   should this segment be playing now?
   */
  isPlaying() {
    return this.shouldBePlaying() &&
      null !== this.source &&
      PLAYING === this.state;
  }

  /**
   should this segment be playing now?
   */
  shouldBePlaying() {
    let currentTime = this.getCurrentTime();
    return 0 < currentTime ? this.beginAtTime < currentTime && this.endAtTime > currentTime : 0;
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
   */F

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
    let text = `    [segment@${this.segment.offset}] ${message}`;
    let info = args && args.length ? args.join(', ') : '';
    console[level](text, ...args);
    let el = document.createElement('p');
    DOM.addClass(el, level);
    DOM.setHTML(el, `${text} ${info}`);
    DOM.prepend(document.getElementById('messages'), el);
  }

  /**
   * Whether this segment audio is sufficiently ahead of the current web audio context time,
   * in order to avoid unnecessary loading, or untenable playback requests.
   * @returns {boolean}
   */
  isFutureEnough() {
    return this.beginAtTime > this.getCurrentTime() + ENFORCE_WEBAUDIO_SCHEDULE_PLAYBACK_AHEAD_SECONDS;
  }

  /**
   The player's current time, in seconds (float precision) since context start
   */
  getCurrentTime() {
    return this.audioContext ? this.audioContext.currentTime : 0;
  }


}

