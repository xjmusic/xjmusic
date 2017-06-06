import Ember from 'ember';

export function capitalize(params/*, hash*/) {
  let string = params[0];
  if (string !== undefined && string !== null && string.length > 0) {
    return string.charAt(0).toUpperCase() + string.slice(1);
  } else {
    return '';
  }
}

export default Ember.Helper.helper(capitalize);
