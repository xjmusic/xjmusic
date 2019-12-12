// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';

export default Model.extend({
  density: attr('number'),
  name: attr('string'),
  type: attr('string'),
  state: attr('string'),
  user: belongsTo('user'),
  library: belongsTo('library'),
  instrumentMemes: hasMany('instrument-meme'),
  audios: hasMany('instrument-audio'),
  audioChords: hasMany('instrument-audio-chord'),
  audioEvents: hasMany('instrument-audio-event'),
  segmentChoiceArrangements: hasMany('segment-arrangement'),
});
