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
  instrumentAudios: hasMany('instrument-audio'),
  instrumentAudioChords: hasMany('instrument-audio-chord'),
  instrumentAudioEvents: hasMany('instrument-audio-event'),
  segmentChoiceArrangements: hasMany('segment-choice-arrangement'),
});
