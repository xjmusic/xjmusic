import DS from 'ember-data';

export default DS.Model.extend({
  density: DS.attr('number'),
  key: DS.attr('string'),
  idea: DS.belongsTo({}),
  name: DS.attr('string'),
  tempo: DS.attr('number'),
  offset: DS.attr('number'),
  total: DS.attr('number'),
});
