import DS from 'ember-data';

export default DS.Model.extend({
  audio: DS.belongsTo({}),
  name: DS.attr('string'),
  position: DS.attr('number'),
});
