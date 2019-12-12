// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {belongsTo} from '@ember-data/model';

export default Model.extend({
  choice: belongsTo('segment-choice'),
  instrument: belongsTo('instrument'),
  segment: belongsTo('segment'),
  voice: belongsTo('program-voice'),
});










