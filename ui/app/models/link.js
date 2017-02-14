import DS from 'ember-data';

export default DS.Model.extend({
  chain: DS.belongsTo({}),
  offset: DS.attr('number'),
  state: DS.attr('string'),
  beginAt: DS.attr('string'),
  endAt: DS.attr('string'),
  total: DS.attr('number'),
  density: DS.attr('number'),
  key: DS.attr('string'),
  tempo: DS.attr('number'),
});

