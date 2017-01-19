import DS from 'ember-data';

export default DS.Model.extend({
  account: DS.belongsTo({}),
  user: DS.belongsTo({})
});
