// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

const rgxValidUrl = /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;

/**
 LinkAudio object, a wrapper to play the audio for a single link in a chain.

 Manages this link audio's buffer and playback in the audio context

 Required to be injected on construction:

 * audioContext
 * binaryResource
 * linkId
 * waveformUrl
 * beginTime
 * endTime

 */
export default Ember.Object.extend({

  // REQUIRED
  audioContext: null,
  binaryResource: null,
  linkId: null,
  linkOffset: null,
  waveformUrl: '',
  beginTime: 0,
  timeOffset: 0,
  endTime: 0,

  // To load audio into, for playback
  bufferSource: null,

  /**
   New LinkAudio
   */
  init() {
    if (this.hasValidWaveformUrl()) {
      this.loadAudio(() => {
        this.playWebAudio();
      });
    } else {
      this.error('invalid waveform url: ' + this.get('waveformUrl'));
    }
  },

  /**
   tend to each active link audio buffer
   */
  playWebAudio() {
    // cannot begin before 0
    if (this.get('beginTime') < 0) {
      this.warn('cannot play WebAudio', this,
        'at beginTime', this.get('beginTime'));
      return;
    }

/*
    // cannot have offset
    if (this.get('timeOffset') > 0) {
      this.warn('currently, playing WebAudio from offset is not functioning as designed! There will be a delay playing the first link. See: https://trello.com/c/8MUci0yz',
        'currentTime', this.get('audioContext').currentTime,
        'beginTime', this.get('beginTime'),
        'timeOffset', this.get('timeOffset'));
      return;
    }
*/

    // sound starts precisely via direct message sent to WebAudio
    this.get('bufferSource').start(this.get('beginTime'), this.get('timeOffset'));

    // log
    this.debug('playing WebAudio',
      'currentTime', this.get('audioContext').currentTime,
      'beginTime', this.get('beginTime'),
      'timeOffset', this.get('timeOffset'));
  },

  /**
   Sound stops
   */
  stopWebAudio() {
    try {
      this.get('bufferSource').stop();
    } catch (e) {
      // noop
    }
  },

  /**
   load waveforms into WebAudio buffers

   @param onSuccess
   */
  loadAudio(onSuccess) {
    let self = this;

    this.debug('loading buffer source...');
    self.get('binaryResource').sendXHR('GET', self.get('waveformUrl'))
      .then((audioData) => {
          self.get('audioContext').decodeAudioData(audioData, function (buffer) {
              let bufferSource = self.get('audioContext').createBufferSource();
              bufferSource.buffer = buffer;
              bufferSource.connect(self.get('audioContext').destination);
              self.set('bufferSource', bufferSource);
              self.debug('loaded buffer source');
              onSuccess();
            },
            (error) => {
              self.error('Failed to decode audio data', error);
            });
        },
        (error) => {
          self.error('Failed to load link audio buffer', error);
        }
      );
  },

  /**
   has valid waveform url?
   */
  hasValidWaveformUrl() {
    return rgxValidUrl.test(this.get('waveformUrl'));
  },

  /**
   should this link be playing now?
   */
  isPlaying() {
    return this.get('beginTime') < this.get('audioContext').currentTime &&
      this.get('endTime') > this.get('audioContext').currentTime;
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
    console[level]('[player-link-' + this.get('linkOffset') + '] ' + message, ...args);
  },

});
