import DS from 'ember-data';

export default DS.Model.extend({
  phase: DS.belongsTo({}),
  name: DS.attr('string'),
});
