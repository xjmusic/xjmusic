import DS from 'ember-data';

export default DS.Model.extend({
  library: DS.belongsTo({}),
  user: DS.belongsTo({}),
  density: DS.attr('number'),
  description: DS.attr('string'),
  type: DS.attr('string'),
});
