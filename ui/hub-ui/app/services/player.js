//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import RSVP from "rsvp";
import Service, {inject as service} from '@ember/service';

/**
 State constants
 * @type {string}
 */
const STANDBY = 'Standby';
const PLAYING = 'Playing';

/**
 Seconds between Main interval cycles
 * @type {number}
 */
const CYCLE_INTERVAL_SECONDS = 1;

/**
 Player service
 */
export default Service.extend({

  // State, Chain Segment
  state: STANDBY,

  // Inject: Configuration
  config: service(),

  // Inject: Ember Data Store
  store: service(),

  // Inject: flash message service
  display: service(),

  // Inject: segment scroll service
  segmentScroll: service(),

  // Base URL of segment waveforms
  segmentBaseUrl: '',

  // now-playing chain
  currentChain: null,

  // now-playing segment
  currentSegment: null,

  // all active segments
  activeSegments: [],

  // # millis UTC, from which to play
  playFromMillisUTC: 0,

  // # millis to offset now during playback
  playOffsetNowMillisUTC: 0,

  // time (in the WebAudio context), from which to play
  playFromContextTime: 0,

  // interval to store Main cycle
  cycleMainInterval: null,

  // interval to store Sub Cycle
  cycleSubInterval: null,

  /**
   New Player
   Load configuration
   */
  init() {
    this._super(...arguments);
    let self = this;
    this.config.getConfig().then(
      () => {
        self.set('segmentBaseUrl', self.config.segmentBaseUrl);
        self.startCycle();
      },
      (error) => {
        console.error('Failed to load config', error);
      }
    );
  },

  /**
   play a chain beginning at a certain segment
   start the interval cycles

   @param chain to play
   @param segment to play chain from
   */
  play(chain, segment) {
    let self = this;

    // stop playback (which also incurs a delay)
    self.stop().then(() => {

        // set the new play-from data
        self.set('playFromMillisUTC', self.computePlayFromMillisUTC(chain, segment));
        self.set('playOffsetNowMillisUTC', Date.now() - self.get('playFromMillisUTC'));

        // set the chain+segment play request
        self.set('currentChain', chain);
        self.set('currentSegment', segment);

        // now playing
        self.set('state', PLAYING);

        // do the cycle now
        self.doCycle();
      },

      (error) => {
        console.error('failed to stop & start playback', error);
      });
  },
  /**
   Stop playback
   @returns {RSVP.Promise}
   */
  stop() {
    let self = this;
    return new RSVP.Promise((resolve) => {
      // set the chain+segment play request
      self.set('currentChain', null);
      self.set('currentSegment', null);

      // now playing
      self.set('state', STANDBY);

      resolve();
    });
  },

  /**
   Do Main Cycle every N seconds
   */
  doCycle() {
    if (this.nonNull(this.currentChain)) {
      this.refreshDataThenUpdate();
    }
  },

  /**
   Check if there is a current chain, and if so, refresh it
   */
  refreshDataThenUpdate() {
    let currentChain = this.currentChain;
    if (this.nonNull(currentChain)) {
      this.update();
    }
  },

  /**
   Update all segment audios
   */
  update() {
    let self = this;
    let segments = this.currentChain.get('segments');
    let nowAtMillisUTC = this.nowMillisUTC();
    segments.forEach((segment) => {
      if (Date.parse(segment.get('beginAt')) < nowAtMillisUTC && Date.parse(segment.get('endAt')) > nowAtMillisUTC) {
        self.set('currentSegment', segment);
      }
    });
    this.scrollToNowPlayingSegment();
  },

  /**
   Update `currentSegment`
   */
  setNowPlayingSegment: function (segment) {
    // return if no change
    let previousSegment = this.currentSegment;
    if (previousSegment && previousSegment.get('id') === segment.get('id')) {
      return;
    }

    this.set('currentSegment', segment);
  },

  /**
   Scroll to the now-playing segment
   */
  scrollToNowPlayingSegment: function () {
    let currentSegment = this.currentSegment;
    if (currentSegment) {
      this.segmentScroll.scrollTo(currentSegment);
    }
  },

  /**
   start the main cycle interval, and sub cycle interval
   */
  startCycle() {
    let self = this;

    self.doCycle();

    self.set('cycleMainInterval', setInterval(function () {
      self.doCycle();
    }, CYCLE_INTERVAL_SECONDS * MILLIS_PER_SECOND));

  },

  /**
   Seconds from UTC to the given date string
   * @param {String} fromTimeUTC
   */
  millisUTC(fromTimeUTC) {
    if (this.nonNull(fromTimeUTC)) {
      return Date.parse(fromTimeUTC);
    } else {
      return null;
    }
  },

  /**
   Seconds from UTC to now
   */
  nowMillisUTC() {
    let nowAdjustedMillisUTC = this.playOffsetNowMillisUTC;
    if (nowAdjustedMillisUTC > 0 || nowAdjustedMillisUTC < 0) {
      return Date.now() - nowAdjustedMillisUTC;
    } else {
      return Date.now();
    }
  },

  /**
   The player's current time, in seconds (float precision) since context start
   */
  currentTime() {
    let fromMillisUTC = this.playFromMillisUTC;
    if (fromMillisUTC > 0) {
      return (this.nowMillisUTC() - fromMillisUTC) / MILLIS_PER_SECOND;
    } else {
      return 0;
    }
  },

  /**
   Whether the object is non-null
   * @param obj
   * @returns {boolean}
   */
  nonNull: function (obj) {
    return !this.isNull(obj);
  },

  /**
   Whether the object is null
   * @param obj
   * @returns {boolean}
   */
  isNull: function (obj) {
    return obj === undefined || obj === null;
  },

  /**
   Compute play-from-seconds UTC depending on the request to play
   */
  computePlayFromMillisUTC(chain, segment) {
    if (this.isNull(chain)) {
      console.debug("player received null chain");
      return null;
    }

    if (this.nonNull(segment)) {
      console.debug("player will play from segment", segment.get('offset'));
      return this.millisUTC(segment.get('beginAt'));
    }

    let chainStartAtMillisUTC = this.millisUTC(chain.get('startAt'));
    let chainStopAtMillisUTC = this.millisUTC(chain.get('stopAt'));
    let nowAtMillisUTC = this.nowMillisUTC();

    switch (chain.get('type').toLowerCase()) {

      case 'production':

        if (isNaN(chainStopAtMillisUTC)) {
          console.debug("player will play production chain from now", "millis UTC", nowAtMillisUTC);
          return nowAtMillisUTC;
        }

        console.debug("player received chain with stop-at", "millis UTC", chainStopAtMillisUTC);

        if (chainStopAtMillisUTC > nowAtMillisUTC) {
          console.debug("player will play production chain from now", "millis UTC", nowAtMillisUTC);
          return nowAtMillisUTC;
        }

        console.debug("player will play production chain from beginning", "millis UTC", chainStartAtMillisUTC);
        return chainStartAtMillisUTC;

      case 'preview':
        console.debug("player will play preview chain", "millis UTC", chainStartAtMillisUTC);
        return chainStartAtMillisUTC;

      default:
        return null;
    }
  },

});

const MILLIS_PER_SECOND = 1000;
