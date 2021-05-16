/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';
import {computed} from '@ember/object';

export default Model.extend({
  name: attr('string'),
  config: attr('string'),
  state: attr('string'),
  type: attr('string'),
  startAt: attr('string', {defaultValue: 'now'}),
  stopAt: attr('string', {defaultValue: ''}),
  fabricatedAheadSeconds: attr('number'),
  embedKey: attr('string', {defaultValue: ''}),
  account: belongsTo('account'),
  chainBindings: hasMany('chain-binding'),
  labUrl: computed('id', function () {
    return `/chains/${this.id}`;
  }),
});

