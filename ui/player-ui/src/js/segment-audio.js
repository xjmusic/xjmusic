// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import {BinaryResource} from 'binary-resource';

/**
 * RegExp to test for a valid URL
 * @type {RegExp}
 */
const rgxValidUrl = /(http|https):\/\/(\w+:?\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@\-\/]))?/;

/**
 * # of seconds of silence to enforce from beginning (0.0 seconds) of WebAudio context
 */
const enforceSilenceSecondsStart = 1;

/**
 * self evident maths
 */
const MILLIS_PER_SECOND = 1000;

/**
 SegmentAudio object, a wrapper to play the audio for a single segment in a chain.

 Manages this segment audio's buffer and playback in the audio context

 Required to be injected on construction:

 * audioContext
 * binaryResource
 * segmentId
 * waveformUrl
 * beginTime
 * endTime

 */
export class SegmentAudio {

  /**
   * @type {Object} HTML5 Web Audio context
   */
  audioContext = null;

  /**
   * @type {String} segmentBaseUrl for segment MP3's
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
  bufferSource = null;

  /**
   * Time (seconds, floating point) to begin playback in WebAudio context
   */
  beginAtTime;

  /**
   * Time (seconds, floating point) to begin playback in WebAudio context
   */
  endAtTime;

  /**
   * Time (seconds,floating point) to offset playback within source audio
   * (in order to begin playback from somewhere in the middle of the source audio)
   */
  timeOffset = 0;

  /**
   * Instantiate a new Segment Audio playback controller
   * @param {AudioContext} audioContext shared by all segments currently playing in this chain
   * @param {number} audioContextStartMillisUTC
   * @param {object} segment to play
   * @param {string} segmentBaseUrl
   */
  constructor(audioContext, audioContextStartMillisUTC,
              segment, segmentBaseUrl) {
    this.audioContext = audioContext;
    this.segmentBaseUrl = segmentBaseUrl;
    this.segment = segment;
    let self = this;

    // compute time-related properties
    self.beginAtTime = (Date.parse(self.segment.beginAt) - audioContextStartMillisUTC) / MILLIS_PER_SECOND;
    self.endAtTime = (Date.parse(self.segment.endAt) - audioContextStartMillisUTC) / MILLIS_PER_SECOND;
    if (self.beginAtTime < enforceSilenceSecondsStart) {
      self.timeOffset = enforceSilenceSecondsStart - self.beginAtTime;
      self.beginAtTime = enforceSilenceSecondsStart;
    }

    if (!self.hasValidWaveformUrl()) {
      self.error('invalid waveform url: ' + self.waveformUrl());
      return;
    }

    self.info('will play', self.waveformUrl(),
      'at', self.segment.beginAt);
    self.loadAudio(() => {
      self.playWebAudio();
    });
  }

  /**
   tend to each active segment audio buffer
   sound starts precisely via direct message sent to WebAudio
   */
  playWebAudio() {
    if (this.timeOffset < this.bufferSource.buffer.duration) {
      this.bufferSource.start(this.beginAtTime, this.timeOffset);
    }
  }

  /**
   Sound stops
   */
  stopWebAudio() {
    try {
      this.bufferSource.stop();
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

    BinaryResource.send('GET', self.waveformUrl(), (audioData) => {
        self.audioContext.decodeAudioData(audioData).then((buffer) => {
            let bufferSource = self.audioContext.createBufferSource();
            bufferSource.buffer = buffer;
            bufferSource.connect(self.audioContext.destination);
            self.bufferSource = bufferSource;
            self.debug('loaded buffer source');
            onSuccess();
          },
          (error) => {
            self.error('Failed to decode audio data', error);
          });
      },
      (error) => {
        self.error('Failed to load segment audio buffer', error);
      }
    );
  }

  /**
   has valid waveform url?
   */
  hasValidWaveformUrl() {
    return rgxValidUrl.test(this.waveformUrl());
  }

  /**
   * @return {string} url of waveform
   */
  waveformUrl() {
    return this.segmentBaseUrl + this.segment.waveformKey;
  }

  /**
   should this segment be playing now?
   */
  isPlaying() {
    return this.beginAtTime < this.audioContext.currentTime &&
      this.endAtTime > this.audioContext.currentTime;
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
    console[level]('    [segment@' + this.segment.offset + '] ' + message, ...args);
  }

}

