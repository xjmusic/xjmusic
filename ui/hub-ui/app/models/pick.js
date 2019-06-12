//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo} from '@ember-data/model';

export default Model.extend({
  segment: belongsTo({}),
  arrangement: belongsTo('arrangement'),
  audio: belongsTo('audio'),
  patternEvent: belongsTo('pattern-event'),
  voice: belongsTo('voice'),
  start: attr('number'),
  length: attr('number'),
  amplitude: attr('number'),
  pitch: attr('number'),
  inflection: attr('string'),
});










