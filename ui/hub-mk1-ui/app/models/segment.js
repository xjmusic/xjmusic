// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';
import {computed} from '@ember/object';

const ROW_HEIGHT_PIXELS_PER_BEAT = 9;

export default Model.extend({
  offset: attr('number'),
  state: attr('string'),
  beginAt: attr('string'),
  endAt: attr('string'),
  total: attr('number'),
  density: attr('number'),
  key: attr('string'),
  tempo: attr('number'),
  waveformKey: attr('string'),
  waveformPreroll: attr("number"),
  chain: belongsTo('chain'),
  segmentMessages: hasMany('segment-message'),
  segmentMemes: hasMany('segment-meme'),
  segmentChoices: hasMany('segment-choice'),
  segmentChoiceArrangements: hasMany('segment-choice-arrangement'),
  segmentChords: hasMany('segment-chord'),

  title: computed('offset', function () {
    return `Segment@${this.offset}`;
  }),

  rowHeight: computed('total', function () {
    let pixels = this.total * ROW_HEIGHT_PIXELS_PER_BEAT;
    return `${pixels}px`;
  }),

});

