// import Ember from 'ember';
import Model from 'ember-data/model';
import attr from 'ember-data/attr';

export default Model.extend({

  type: attr('string'),
  state: attr('string'),
  name: attr('string'),
  chainOffset: attr('number'),
  chainId: attr('number'),

  songMain: attr('string'),
  beatMain: attr('string'),
  songOut: attr('string'),
  songIn: attr('string'),
  signature: attr('string'),
  feel: attr('string'),
  beatOut: attr('string'),
  beatIn: attr('string'),

});
