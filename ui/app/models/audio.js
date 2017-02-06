import DS from 'ember-data';

export default DS.Model.extend({
  instrument: DS.belongsTo({}),
  name: DS.attr('string'),
  waveformUrl: DS.attr('string'),
  start: DS.attr('number'),
  length: DS.attr('number'),
  tempo: DS.attr('number'),
  pitch: DS.attr('number'),
});
