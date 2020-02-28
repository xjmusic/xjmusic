/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import Model, {attr, belongsTo} from '@ember-data/model';
import {computed} from '@ember/object';

export default Model.extend({
  duration: attr('number'),
  note: attr('string'),
  position: attr('number'),
  velocity: attr('number'),
  programSequencePattern: belongsTo('program-sequence-pattern'),
  programVoiceTrack: belongsTo('program-voice-track'),

  title: computed('note', 'position', function () {
    return `${this.note}@${this.position}`;
  }),

});
