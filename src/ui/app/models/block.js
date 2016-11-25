import Model from 'ember-data/model';
import attr from 'ember-data/attr';

export default Model.extend({

  songOut: attr('string'),
  songIn: attr('string'),
  signature: attr('string'),
  feel: attr('string'),
  beatOut: attr('string'),
  beatIn: attr('string')

});
