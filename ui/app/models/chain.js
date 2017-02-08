import DS from 'ember-data';

export default DS.Model.extend({
  account: DS.belongsTo({}),
  name: DS.attr('string'),
  state: DS.attr('string'),
  startAt: DS.attr('string'),
  stopAt: DS.attr('string'),
});

