import DS from 'ember-data';

export default DS.Model.extend({
  instrument: DS.belongsTo({}),
  name: DS.attr('string'),
});
