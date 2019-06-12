//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';

export default Model.extend({
  account: belongsTo({}),
  name: attr('string'),

  programs: hasMany('program'),
  instruments: hasMany('instrument'),
});

