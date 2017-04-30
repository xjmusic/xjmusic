import Ember from "ember";

const {
  Component,
  computed,
  copy,
  getWithDefault,
  assert,
  getOwner
} = Ember;
const {
  readOnly
} = computed;

export default Component.extend({
  tagName: 'div',
  linkable: true,
  currentUrl: readOnly('applicationRoute.router.url'),
  currentRouteName: readOnly('applicationRoute.controller.currentRouteName'),
  headline: computed('currentUrl', 'currentRouteName', {
    get() {
      const currentRouteName = getWithDefault(this, 'currentRouteName', false);

      assert('[ember-crumbly] Could not find a current route', currentRouteName);

      return copy(getWithDefault(this._lookupRoute(currentRouteName), 'routeHeadline', {
        title: name
      }));
    }
  }).readOnly(),

  _guessRoutePath(routeNames, name, index) {
    const routes = routeNames.slice(0, index + 1);

    if (routes.length === 1) {
      let path = `${name}.index`;

      return (this._lookupRoute(path)) ? path : name;
    }

    return routes.join('.');
  },

  _lookupRoute(routeName) {
    return getOwner(this).lookup(`route:${routeName}`);
  },

});
