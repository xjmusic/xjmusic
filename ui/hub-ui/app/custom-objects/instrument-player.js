//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import EmberObject, {get, set} from '@ember/object';
import {all, task} from 'ember-concurrency';
import {Promise as EmberPromise} from 'rsvp';
import $ from 'jquery';
/* global ArrayBuffer */

// Configurations
const TASK_MAX_CONCURRENCY = 6;
const MATCH_AUDIO_INFLECTION_THRESHOLD_MAX = 4;

// States
const Initial = 'Standby';
const Loading = 'Loading';
const Playing = 'Playing';
const Failed = 'Failed';

/**
 Instrument player
 */
const InstrumentPlayer = EmberObject.extend({
  // Name
  name: 'InstrumentPlayer',

  // State
  state: Initial,

  // Instrument
  instrument: null,

  // Buffer Sources, keyed by audioId
  audioBufferCache: {},

  // Cache of audio matching particular inflection, array of audioId keyed by inflection
  matchAudioIdsCache: {},

  // Cache of audioEvent inflections, array of audioId keyed by inflection
  inflectionAudioIdsCache: {},

  // Base URL of audio waveform files
  audioBaseUrl: '',

  // keep active main task here for cancellation
  mainTask: null,

  // keep active sub-tasks here for progress monitor or cancellation
  subTasks: [],

  /**
   * Go to the specified state.
   * Calls hooks depending on what state we are moving from/to
   * @param {String} targetState for transition into
   */
  goToState: function (targetState) {
    this.debug('go to state', targetState);
    set(this, 'state', targetState);
    switch (targetState) {
      case Initial:
        // nothing special
        break;
      case Loading:
        // nothing special
        break;
      case Playing:
        // nothing special
        break;
      case Failed:
        this.error('failed!');
        this.destroyAudioContext();
        break;
    }
  },

  /**
   setup an instrument for playback, specifically the audio within it

   @param {EmberObject} instrument to setup for playback
   @param {[EmberObject]} audios array of audios to setup for playback
   @param {[EmberObject]} audioEvents array of audio events to setup for playback
   @param {Function} loadingProgressCallbackFn to callback with progress ratio from 0 to 1
   @return {EmberPromise} promise resolved when playing has begun
   */
  setup(instrument, audios, audioEvents, loadingProgressCallbackFn) {
    set(this, 'instrument', instrument);
    set(this, 'audios', audios);
    set(this, 'audioEvents', audioEvents);
    set(this, 'matchAudioIdsCache', {});
    set(this, 'inflectionAudioIdsCache', {});
    set(this, 'audioBufferCache', {});
    this.goToState(Loading);
    this.createAudioContext();

    // create the main task and persist it
    let mainTask = this.instrumentLoadTask.perform(loadingProgressCallbackFn);
    set(this, 'mainTask', mainTask);

    // wrap the completion of the main task in the resolution of this promise
    let self = this;
    return new EmberPromise((resolve, reject) => {
      mainTask.then(
        () => {
          self.goToState(Playing);
          resolve();
        },
        () => {
          self.goToState(Failed);
          reject();
        });
    });
  },

  /**
   * stop playing
   @return {EmberPromise} promise resolved when stopping has completed
   */
  stop() {
    let self = this;
    return new EmberPromise((resolve, reject) => {
      get(self, 'subTasks').forEach((task) => {
        task.cancel();
      });
      get(self, 'mainTask').cancel();
      self.destroyAudioContext().then(resolve, reject);
    });
  },

  /**
   displays loading progress
   Sends a cascade of HTTP requests to load audio waveform files
   @param {EmberObject} instrument to load
   @param {Function} progressCallbackFn to callback with progress ratio from 0 to 1
   */
  instrumentLoadTask: task(function* (progressCallbackFn) {
    let audios = get(this, 'audios');
    let self = this;
    let subTasks = [];

    // task for each audio
    audios.forEach((audio) => {
      subTasks.push(self.audioLoadTask.perform(audio, progressCallbackFn));
    });

    // manually resolve after all subTasks are complete, in order to clear the progress interval
    set(this, 'subTasks', subTasks);
    yield all(subTasks);
  }),

  /**
   load an audio buffer source, as child to concurrent task group
   @param {EmberObject} audio to load
   @param {Function} progressCallbackFn to callback with progress ratio from 0 to 1
   */
  audioLoadTask: task(function* (audio, progressCallbackFn) {
    let self = this;
    let audioContext = get(this, 'audioContext');
    yield new EmberPromise((resolve, reject) => {
      self.addInflectionToAudioIdsCache(audio);
      let waveformUrl = self.waveformUrl(audio);
      if (Loading === get(self, 'state')) {
        sendBinaryXHR('GET', waveformUrl, (audioData) => {
          audioContext.decodeAudioData(audioData).then((buffer) => {
            get(self, 'audioBufferCache')[audio.get('id')] = buffer;
            self.sendTasksProgress(progressCallbackFn);
            resolve();
          }, reject);
        }, reject);
      } else {
        reject();
      }
    });
  }).enqueue().maxConcurrency(TASK_MAX_CONCURRENCY),

  /**
   Send the progress update to the callback function
   by checking on the doneness of all sub-tasks
   @param {Function} progressCallbackFn to callback with progress ratio from 0 to 1
   */
  sendTasksProgress: function (progressCallbackFn) {
    let finishedCount = 0;
    let subTasks = get(this, 'subTasks');
    subTasks.forEach((task) => {
      if (task.isFinished) {
        finishedCount++;
      }
    })
    progressCallbackFn(finishedCount / subTasks.length);
  },

  /**
   Add this audio to the cache of audioId for each inflection

   @param {EmberObject} audio to add
   */
  addInflectionToAudioIdsCache(audio) {
    let cache = get(this, 'inflectionAudioIdsCache');
    let audioEvents = get(this, 'audioEvents');
    let inflection = '';
    audioEvents.forEach((audioEvent) => {
      if (audio.get('id') === audioEvent.get('audio').get('id')) {
        inflection += audioEvent.get('inflection');
      }
    });
    if (!(inflection in cache)) {
      cache[inflection] = [];
    }
    cache[inflection].push(audio.get('id'));
  },

  /**
   * @param {EmberObject} audio to compute waveform URL for
   * @returns {string} Waveform URL
   */
  waveformUrl(audio) {
    return get(this, 'audioBaseUrl') + audio.get('waveformKey');
  },

  /**
   * Search for audios matching the provided inflection
   * @param matchInflection to search for
   * @returns {[EmberObject]} audios matching inflection
   */
  matchAudioIds(matchInflection) {
    let matchAudioIdsCache = get(this, 'matchAudioIdsCache');
    let inflectionAudioIdsCache = get(this, 'inflectionAudioIdsCache');
    if (!(matchInflection in matchAudioIdsCache)) {
      matchAudioIdsCache[matchInflection] = [];
      for (let searchInflection in inflectionAudioIdsCache) {
        if (inflectionAudioIdsCache.hasOwnProperty(searchInflection)) {
          if (MATCH_AUDIO_INFLECTION_THRESHOLD_MAX >
            similarity(matchInflection, searchInflection)) {
            inflectionAudioIdsCache[searchInflection].forEach((audioId) => {
              matchAudioIdsCache[matchInflection].push(audioId)
            });
          }
        }
      }
    }
    return matchAudioIdsCache[matchInflection];
  },

  /**
   * Select audio buffer from all available audios
   * by comparing inflection to available audio inflections
   * @param inflection
   * @param velocity
   */
  createBufferSourceRandomlyForInflection(inflection, velocity) {
    let options = this.matchAudioIds(inflection);
    let audioBufferCache = get(this, 'audioBufferCache');
    let audioContext = get(this, 'audioContext');
    let audioId = options[Math.floor(Math.random() * options.length)];
    let bufferSource = audioContext.createBufferSource();
    let gainNode = audioContext.createGain();
    gainNode.gain.value = velocity;
    gainNode.connect(audioContext.destination);
    bufferSource.buffer = audioBufferCache[audioId];
    bufferSource.connect(gainNode);
    return bufferSource;
  },

  /**
   * Schedule audio for playback
   * Duration should have been re-computed when the events in the stepmatic were updated
   *
   * @param {String} inflection to search audio for candidate
   * @param {Number} velocity of event
   * @param {Number} playAudioAt for audio playback
   * @param {Number} playAudioDuration for audio playback
   */
  scheduleAudio(inflection, velocity, playAudioAt, playAudioDuration) {
    this.debug('scheduleAudio(inflection:' + inflection + ', velocity:' + velocity + ' playAudioAt:' + playAudioAt + ' playAudioDuration:' + playAudioDuration + '');
    this.createBufferSourceRandomlyForInflection(inflection, velocity).start(playAudioAt, 0, playAudioDuration);
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
    let audioContext;
    if (window.AudioContext) {
      audioContext = new window.AudioContext();
    } else {
      audioContext = new window.webkitAudioContext();
    }

    // create empty buffer
    let buffer = audioContext.createBuffer(1, 1, 22050);
    let dryFire = audioContext.createBufferSource();
    dryFire.buffer = buffer;

    // connect to output (your speakers)
    dryFire.connect(audioContext.destination);

    // play the file
    dryFire.start(0);

    // persist the audio context
    set(this, 'audioContext', audioContext);
  },

  /**
   * @return {Number} current time in web audio context
   */
  currentTime() {
    let audioContext = get(this, 'audioContext');
    return audioContext.currentTime;
  },

  /**
   * Destroy WebAudio context
   * @return {EmberPromise} promise resolved after context has been destroyed
   */
  destroyAudioContext() {
    let audioContext = get(this, 'audioContext');
    let self = this;
    return new EmberPromise((resolve, reject) => {
      if (nonNull(audioContext)) {
        if ('closed' !== audioContext.state) {
          audioContext.close().then(() => {
            set(self, 'audioContext', null);
            resolve();
          }, reject);
        } else {
          resolve();
        }
      } else {
        resolve();
      }
    });
  }

});

/**
 * Is an object non-null?
 * @param obj to check
 * @returns {boolean} true if non-null
 */
function nonNull(obj) {
  return !isNull(obj);
}

/**
 * Is an object null?
 * @param obj to check
 * @returns {boolean} true if null
 */
function isNull(obj) {
  return obj === undefined || obj === null;
}

/**
 * This will be added to jQuery when this class is loaded
 * use this transport for 'binary' data type
 */
$['ajaxTransport']('+binary', function (options, originalOptions, jqXHR) {
  // check for conditions and support for blob / arraybuffer response type
  if (window.FormData && ((options.dataType && (options.dataType === 'binary')) || (options.data && ((window.ArrayBuffer && options.data instanceof ArrayBuffer) || (window.Blob && options.data instanceof Blob))))) {
    return {
      // create new XMLHttpRequest
      send: function (headers, callback) {
        // setup all variables
        let xhr = new XMLHttpRequest(),
          url = options.url,
          type = options.type,
          async = options.async || true,
          // blob or arraybuffer. Default is blob
          dataType = options.responseType || 'blob',
          data = options.data || null,
          username = options.username || null,
          password = options.password || null;

        xhr.addEventListener('load', function () {
          let data = {};
          data[options.dataType] = xhr.response;
          // make callback and send data
          callback(xhr.status, xhr.statusText, data, xhr.getAllResponseHeaders());
        });

        xhr.open(type, url, async, username, password);

        // setup custom headers
        for (let i in headers) {
          if (headers.hasOwnProperty(i)) {
            xhr.setRequestHeader(i, headers[i]);
          }
        }

        xhr.responseType = dataType;
        xhr.send(data);
      },
      abort: function () {
        jqXHR.abort();
      }
    };
  }
});

/**
 Send GET request for binary data from a URL
 * @param method for request
 * @param url for request
 * @param {Function} onSuccess callback
 * @param {Function} onError callback
 * @returns {*} promise to return binary data
 */
function sendBinaryXHR(method, url, onSuccess, onError) {
  $['ajax']({
    url: url,
    dataType: 'binary',
    responseType: 'arraybuffer',
    processData: false,
    method: method,
    success: onSuccess,
    error: onError
  });
}

/**
 * Measure the difference between two strings
 * with the fastest JS implementation of the
 * Levenshtein distance algorithmv
 *
 * @see https://github.com/sindresorhus/leven
 */
function similarity(a, b) {
  if (a === b) {
    return 0;
  }

  let swap = a;

  // Swapping the strings if `a` is longer than `b` so we know which one is the
  // shortest & which one is the longest
  if (a.length > b.length) {
    a = b;
    b = swap;
  }

  let aLen = a.length;
  let bLen = b.length;

  // Performing suffix trimming:
  // We can linearly drop suffix common to both strings since they
  // don't increase distance at all
  // Note: `~-` is the bitwise way to perform a `- 1` operation
  while (aLen > 0 && (a.charCodeAt(~-aLen) === b.charCodeAt(~-bLen))) {
    aLen--;
    bLen--;
  }

  // Performing prefix trimming
  // We can linearly drop prefix common to both strings since they
  // don't increase distance at all
  let start = 0;

  while (start < aLen && (a.charCodeAt(start) === b.charCodeAt(start))) {
    start++;
  }

  aLen -= start;
  bLen -= start;

  if (aLen === 0) {
    return bLen;
  }

  let bCharCode;
  let ret;
  let tmp;
  let tmp2;
  let i = 0;
  let j = 0;

  while (i < aLen) {
    charCodeCache[i] = a.charCodeAt(start + i);
    arr[i] = ++i;
  }

  while (j < bLen) {
    bCharCode = b.charCodeAt(start + j);
    tmp = j++;
    ret = j;

    for (i = 0; i < aLen; i++) {
      tmp2 = bCharCode === charCodeCache[i] ? tmp : tmp + 1;
      tmp = arr[i];
      ret = arr[i] = tmp > ret ? tmp2 > ret ? ret + 1 : tmp2 : tmp2 > tmp ? tmp + 1 : tmp2;
    }
  }

  return ret;
}

// in-memory working space for similarity()
let arr = [];

// in-memory working space for similarity()
let charCodeCache = [];

// Finally, export the object
export default InstrumentPlayer;
