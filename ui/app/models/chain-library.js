import DS from 'ember-data';

export default DS.Model.extend({
  chain: DS.belongsTo({}),
  library: DS.belongsTo({})
});
