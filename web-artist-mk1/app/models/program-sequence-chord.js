/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';
import {computed} from '@ember/object';

export default Model.extend({
  name: attr('string'),
  position: attr('number'),
  programSequence: belongsTo('program-sequence'),
  programSequenceChordVoicings: hasMany('program-sequence-chord-voicing'),

  title: computed('name', 'position', function () {
    return `${this.name}@${this.position}`;
  }),

});
