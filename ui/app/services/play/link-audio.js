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
      console.error('invalid waveform url: ' + this.get('waveformUrl'));
    }
  },

  /**
   tend to each active link audio buffer
   */
  playWebAudio() {
    // cannot begin before 0
    if (this.get('beginTime') < 0) {
      console.warn('cannot play WebAudio', this,
        'at beginTime', this.get('beginTime'));
      return;
    }

    // cannot have offset
    if (this.get('timeOffset')>0) {
     console.warn('currently, playing WebAudio from offset is not functioning as designed! There will be a delay playing the first link. See: https://trello.com/c/8MUci0yz');
     return;
    }
    console.debug('playing WebAudio for link ' + this.get('linkId'),
      'currentTime', this.get('audioContext').currentTime,
      'beginTime', this.get('beginTime'),
      'timeOffset', this.get('timeOffset'));

    // sound starts precisely via direct message sent to WebAudio
    this.get('bufferSource').start(this.get('beginTime'), this.get('timeOffset'));
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

    console.debug('[play] loading buffer source for Link #' + self.get('linkId') + '...');
    self.get('binaryResource').sendXHR('GET', self.get('waveformUrl'))
      .then((audioData) => {
          self.get('audioContext').decodeAudioData(audioData, function (buffer) {
              let bufferSource = self.get('audioContext').createBufferSource();
              bufferSource.buffer = buffer;
              bufferSource.connect(self.get('audioContext').destination);
              self.set('bufferSource', bufferSource);
              console.debug('[play] loaded buffer source for Link #' + self.get('linkId') + '.');
              onSuccess();
            },
            (error) => {
              console.error('Failed to decode audio data for Link # ' + self.get('linkId'), error);
            });
        },
        (error) => {
          console.error('Failed to load link audio buffer', error);
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
  }

});
