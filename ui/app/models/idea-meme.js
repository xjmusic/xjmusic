import DS from 'ember-data';

export default DS.Model.extend({
  idea: DS.belongsTo({}),
  name: DS.attr('string'),
});
