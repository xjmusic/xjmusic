//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';

export default Model.extend({
  user: belongsTo({}),
  library: belongsTo({}),
  density: attr('number'),
  description: attr('string'),
  type: attr('string'),
  state: attr('string'),
  instrumentMemes: hasMany('instrument-meme'),
  audios: hasMany('audio'),
  audioChords: hasMany('audio-chord'),
  audioEvents: hasMany('audio-event'),
  arrangements: hasMany('arrangement'),
});
