import DS from 'ember-data';

export default DS.Model.extend({
  phase: DS.belongsTo({}),
  type: DS.attr('string'),
  description: DS.attr('string'),
});
