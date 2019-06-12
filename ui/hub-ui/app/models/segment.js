//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';
import {computed} from '@ember/object';

const ROW_HEIGHT_PIXELS_PER_BEAT = 9;

export default Model.extend({
  chain: belongsTo({}),
  offset: attr('number'),
  state: attr('string'),
  beginAt: attr('string'),
  endAt: attr('string'),
  total: attr('number'),
  density: attr('number'),
  key: attr('string'),
  tempo: attr('number'),
  waveformKey: attr('string'),

  messages: hasMany('segment-message'),
  memes: hasMany('segment-meme'),
  choices: hasMany('choice'),
  chords: hasMany('segment-chord'),

  title: computed('offset', function () {
    return `Segment@${this.offset}`;
  }),

  rowHeight: computed('total', function () {
    let pixels = this.total * ROW_HEIGHT_PIXELS_PER_BEAT;
    return `${pixels}px`;
  }),

});

