//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';

export default Model.extend({
  account: belongsTo({}),
  name: attr('string'),
  state: attr('string'),
  type: attr('string'),
  startAt: attr('string', {defaultValue: 'now'}),
  stopAt: attr('string', {defaultValue: ''}),
  embedKey: attr('string', {defaultValue: ''}),

  "segments": hasMany('segment'),
  "chain-configs": hasMany('chain-config'),
  "chain-bindings": hasMany('chain-binding'),
});

