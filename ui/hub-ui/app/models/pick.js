// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo} from '@ember-data/model';

export default Model.extend({
  segment: belongsTo({}),
  arrangement: belongsTo('arrangement'),
  audio: belongsTo('audio'),
  event: belongsTo('event'),
  voice: belongsTo('voice'),
  start: attr('number'),
  length: attr('number'),
  amplitude: attr('number'),
  pitch: attr('number'),
  name: attr('string'),
});










